package com.ctsgroup.nl.dashretry.services;

import com.ctsgroup.nl.dashretry.models.Project;
import com.ctsgroup.nl.dashretry.models.User;
import com.ctsgroup.nl.dashretry.repositories.ProjectRepository;
import com.ctsgroup.nl.dashretry.repositories.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


@Service
public class ProjectService {

    Dotenv dotenv = Dotenv.configure().load();
    String apiKey = dotenv.get("ASANA_API_KEY");
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public void updateProjects() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://app.asana.com/api/1.0/projects?workspace=12840025534543&opt_fields=name,completed,permalink_url,created_at,owner,due_date,team.name,color"))
                    .header("accept", "application/json")
                    .header("authorization", "Bearer " + apiKey)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject jsonObject = new JSONObject(response.body());
            if (jsonObject.has("data")) {
                JSONArray dataArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject projectObject = dataArray.getJSONObject(i);

                    Project project = new Project();
                    project.setId(Long.valueOf(projectObject.getString("gid")));
                    project.setProjectName(projectObject.getString("name"));
                    project.setCreatedAt(convertTimeZone(projectObject.getString("created_at")));
                    project.setCompleted(projectObject.getBoolean("completed"));
                    project.setUrl(projectObject.getString("permalink_url"));
                    project.setColor(projectObject.optString("color", null));

                    JSONObject teamObject = projectObject.getJSONObject("team");
                    if(teamObject.has("gid") && teamObject.has("name")){
                        project.setTeamId(Long.valueOf(teamObject.getString("gid")));
                        project.setTeamName(teamObject.getString("name"));
                    }
                    else {
                        project.setTeamId(0L);
                        project.setTeamName("No team");
                    }

                    if (!projectObject.isNull("due_date")) {
                        project.setDueDate(LocalDate.parse(projectObject.getString("due_date"), DateTimeFormatter.ISO_DATE));
                    }
                    else project.setDueDate(null);

                    //Check if owner is present in json
                    if (!projectObject.isNull("owner")) {
                        JSONObject ownerObject = projectObject.getJSONObject("owner");
                        project.setOwnerId(Long.valueOf(ownerObject.getString("gid")));
                    }

                    //Check if owner is present in db
                    User owner = userRepository.getUserById(project.getOwnerId());
                    if (owner == null) {
                        project.setOwnerId(0L);
                    }

                    Project existingProject = projectRepository.getProjectById(project.getId());

                    if (existingProject == null) {
                        projectRepository.save(project);
                    } else {
                        existingProject.setProjectName(project.getProjectName());
                        existingProject.setCreatedAt(project.getCreatedAt());
                        existingProject.setCompleted(project.getCompleted());
                        existingProject.setUrl(project.getUrl());
                        existingProject.setOwnerId(project.getOwnerId());
                        existingProject.setDueDate(project.getDueDate());
                        existingProject.setTeamId(project.getTeamId());
                        existingProject.setTeamName(project.getTeamName());
                        existingProject.setColor(project.getColor());
                        projectRepository.save(existingProject);
                    }
                }
            }
        } catch (IOException | InterruptedException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private LocalDateTime convertTimeZone(String time) {
        // Parse the date-time string into a ZonedDateTime with UTC time zone.
        ZonedDateTime utcDateTime = ZonedDateTime.parse(
                time,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
        );

        // Convert to Amsterdam time zone.
        ZoneId amsterdamZone = ZoneId.of("Europe/Amsterdam");
        ZonedDateTime amsterdamTime = utcDateTime.withZoneSameInstant(amsterdamZone);

        // Return the LocalDateTime in Amsterdam time zone
        return amsterdamTime.toLocalDateTime();
    }
}
