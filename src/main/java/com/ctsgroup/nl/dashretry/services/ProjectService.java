package com.ctsgroup.nl.dashretry.services;

import com.ctsgroup.nl.dashretry.models.Project;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import com.ctsgroup.nl.dashretry.repositories.ProjectRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    Dotenv dotenv = Dotenv.configure().load();
    String apiKey = dotenv.get("ASANA_API_KEY");

    public void updateProjects() {
        try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://app.asana.com/api/1.0/projects?workspace=12840025534543&opt_fields=name,completed,permalink_url,created_at"))
                        .header("accept", "application/json")
                        .header("authorization", "Bearer " + apiKey)
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

                JSONObject jsonObject = new JSONObject(response.body());
            if(jsonObject.has("data")) {
                JSONArray dataArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject projectObject = dataArray.getJSONObject(i);

                    Project project = new Project();
                    project.setId(Long.valueOf(projectObject.getString("gid")));
                    project.setProjectName(projectObject.getString("name"));

                    // Parse the date-time string into a ZonedDateTime with UTC time zone.
                    ZonedDateTime utcDateTime = ZonedDateTime.parse(
                            projectObject.getString("created_at"),
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    );

                    // Convert to Amsterdam time zone.
                    ZoneId amsterdamZone = ZoneId.of("Europe/Amsterdam");
                    ZonedDateTime amsterdamTime = utcDateTime.withZoneSameInstant(amsterdamZone);

                    // Convert to LocalDateTime
                    LocalDateTime amsterdamLocalDateTime = amsterdamTime.toLocalDateTime();

                    project.setCreatedAt(amsterdamLocalDateTime);
                    project.setCompleted(projectObject.getBoolean("completed"));
                    project.setUrl(projectObject.getString("permalink_url"));

                    Project newProject = projectRepository.getProjectById(project.getId());

                    if (newProject == null) {
                        projectRepository.save(project);
                    } else {
                        newProject.setProjectName(project.getProjectName());
                        newProject.setCreatedAt(project.getCreatedAt());
                        newProject.setCompleted(project.getCompleted());
                        newProject.setUrl(project.getUrl());
                        projectRepository.save(newProject);
                    }
                }
            }
        } catch (IOException | InterruptedException | JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
