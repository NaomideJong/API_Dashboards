package com.ctsgroup.nl.dashretry.services;

import com.ctsgroup.nl.dashretry.models.*;
import com.ctsgroup.nl.dashretry.repositories.*;
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
import java.util.Objects;
import java.util.Optional;

@Service
public class UserTimeService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTimeRepository userTimeRepository;

    Dotenv dotenv = Dotenv.configure().load();
    String everhourApiKey = dotenv.get("EVERHOUR_API_KEY");

    String recentDate = LocalDate.now().minusDays(14).toString();

    public void updateUserTimeByUser(String everhourUserId){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.everhour.com/users/" + everhourUserId + "/time?"))//from=" + recentDate))
                    .header("accept", "application/json")
                    .header("X-Api-Key", everhourApiKey)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 404) {
                JSONArray jsonArray = new JSONArray(response.body());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    JSONArray projectIds = setProjectIds(jsonObject);

                    //loop through array of project id's
                    for(int j = 0; j < Objects.requireNonNull(projectIds).length(); j++){

                        UserTime userTime = setUserTime(jsonObject, everhourUserId, projectIds, j);
                        saveUserTime(userTime);

                    }
                }
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveUserTime(UserTime userTime){
        //get UserTime from database by userId, date and projectId
        Optional<UserTime> duplicateUserTime = userTimeRepository.findTopByUserIdAndDateAndTaskId(userTime.getUserId(), userTime.getDate(), userTime.getTaskId());

        if(duplicateUserTime.isPresent()){
            duplicateUserTime.ifPresent(newUserTime -> {
                newUserTime.setTime(userTime.getTime());
                userTimeRepository.save(newUserTime);
            });
        } else {
            userTimeRepository.save(userTime);
        }
    }

    private UserTime setUserTime(JSONObject jsonObject, String everhourUserId, JSONArray projectIds, int j) throws JSONException {
        UserTime userTime = new UserTime();

        //get user by everhour_user_id and check if not null otherwise set 0
        User user = userRepository.findByEverhourUserId(Long.valueOf(everhourUserId));
        if(user != null){
            userTime.setUserId(user.getId());
        } else {
            userTime.setUserId(0L);
        }

        userTime.setDate(LocalDate.parse(jsonObject.getString("date")));
        userTime.setTime(jsonObject.getInt("time"));
        //remove as: prefix from project id before setting the Project ID
        String projectId = projectIds.getString(j).replace("as:", "");
        userTime.setProjectId(Long.valueOf(projectId));

        if(jsonObject.has("task")) {
            JSONObject task = jsonObject.getJSONObject("task");
            userTime.setTaskId(task.getString("id").replace("as:", ""));
            //cut string at 255 characters and make sure there are no weird characters
            if(task.getString("name").length() > 255){
                userTime.setTaskName(task.getString("name").substring(0, 255).replaceAll("[^\\x00-\\x7F]", ""));
            } else {
                userTime.setTaskName(task.getString("name").replaceAll("[^\\x00-\\x7F]", ""));
            }
        }

        return userTime;
    }

    private JSONArray setProjectIds(JSONObject jsonObject) throws JSONException {
        JSONArray projectIds = null;

        if(jsonObject.has("task")){
            JSONObject task = jsonObject.getJSONObject("task");
            //get the "projects" field from the json object, it is not an array
            if(task.has("projects")) projectIds = task.getJSONArray("projects");
        }
        else{
            projectIds = new JSONArray("[0]");
        }

        return projectIds;
    }
}
