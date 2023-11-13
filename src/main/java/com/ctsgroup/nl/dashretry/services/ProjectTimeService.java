package com.ctsgroup.nl.dashretry.services;

import com.ctsgroup.nl.dashretry.repositories.*;
import com.ctsgroup.nl.dashretry.models.ProjectTime;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class ProjectTimeService {

    @Autowired
    private ProjectTimeRepository projectTimeRepository;

    Dotenv dotenv = Dotenv.configure().load();
    String everhourApiKey = dotenv.get("EVERHOUR_API_KEY");
    String recentDate = LocalDate.now().minusDays(14).toString();

    public void updateEverhourProjectTime(String projectId){
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.everhour.com/projects/as:" + projectId + "/time?from=" + recentDate))
                    .header("accept", "application/json")
                    .header("X-Api-Key", everhourApiKey)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            //check if status code is 200 and that the body isn't empty and is an array
            if (response.statusCode() == 200 && !response.body().equals("[]")) {
                String responseBody = response.body();

                if (responseBody.startsWith("[")) {
                    // It's an array
                    JSONArray jsonArray = new JSONArray(responseBody);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        ProjectTime projectTime = setProjectTime(jsonObject, projectId);
                        saveProjectTime(projectTime);
                    }
                } else if (responseBody.startsWith("{")) {
                    // It's a JSON object
                    JSONObject jsonObject = new JSONObject(responseBody);

                        ProjectTime projectTime = setProjectTime(jsonObject, projectId);
                        saveProjectTime(projectTime);

                } else {
                    // Handle other cases or log a warning
                    System.out.println("Unexpected response format: " + responseBody);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveProjectTime(ProjectTime projectTime){
        //get last date from database
        Optional<ProjectTime> latestProjectTime = projectTimeRepository.findTopByProjectIdAndDateAndTaskId(projectTime.getProjectId(), projectTime.getDate(), projectTime.getTaskId());

        if (latestProjectTime.isPresent()) {
            latestProjectTime.get().setTime(projectTime.getTime());
            projectTimeRepository.save(latestProjectTime.get());
        } else {
            projectTimeRepository.save(projectTime);
        }
    }

    private ProjectTime setProjectTime(JSONObject jsonObject, String projectId){
        try{
            ProjectTime projectTime = new ProjectTime();
            projectTime.setProjectId(Long.valueOf(projectId));

            LocalDate date = LocalDate.parse(jsonObject.getString("date"));
            projectTime.setDate(date);

            projectTime.setTime(jsonObject.getInt("time"));

            if(jsonObject.has("task")) {
                JSONObject task = jsonObject.getJSONObject("task");
                projectTime.setTaskId(task.getString("id").replace("as:", ""));
                //cut string at 255 characters and make sure there are no weird characters
                if(task.getString("name").length() > 255){
                    projectTime.setTaskName(task.getString("name").substring(0, 255).replaceAll("[^\\x00-\\x7F]", ""));
                } else {
                    projectTime.setTaskName(task.getString("name").replaceAll("[^\\x00-\\x7F]", ""));
                }
            }

            return projectTime;

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
