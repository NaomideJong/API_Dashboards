package com.ctsgroup.nl.dashretry.services;

import com.ctsgroup.nl.dashretry.models.*;
import com.ctsgroup.nl.dashretry.repositories.*;
import com.ctsgroup.nl.dashretry.models.ProjectTime;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
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

            //check if status code is 200 and that the body isn't empty
            if(response.statusCode() == 200 && !response.body().equals("[]")) {
                JSONArray jsonArray = new JSONArray(response.body());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    ProjectTime projectTime = setProjectTime(jsonObject, projectId);
                    saveProjectTime(projectTime);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveProjectTime(ProjectTime projectTime){
        //get last date from database
        Optional<ProjectTime> latestProjectTime = projectTimeRepository.findTopByProjectIdAndDate(projectTime.getProjectId(), projectTime.getDate());

        if (latestProjectTime.isPresent()) {
            int newTime = latestProjectTime.get().getTime() + projectTime.getTime();
            latestProjectTime.get().setTime(newTime);
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

            return projectTime;

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
