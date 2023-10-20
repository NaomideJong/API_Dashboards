package com.ctsgroup.nl.dashretry.services;

import com.ctsgroup.nl.dashretry.models.EverhourUser;
import com.ctsgroup.nl.dashretry.models.User;
import com.ctsgroup.nl.dashretry.repositories.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    Dotenv dotenv = Dotenv.configure().load();
    String asanaApiKey = dotenv.get("ASANA_API_KEY");
    String everhourApiKey = dotenv.get("EVERHOUR_API_KEY");
    @Autowired
    private UserRepository userRepository;

    public void updateUsers() {
        try {
            List<EverhourUser> everhourUserList = getEverhourUserId();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://app.asana.com/api/1.0/users?opt_fields=email,name"))
                    .header("accept", "application/json")
                    .header("authorization", "Bearer " + asanaApiKey)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject jsonObject = new JSONObject(response.body());

            if (jsonObject.has("data")) {
                JSONArray dataArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject userObject = dataArray.getJSONObject(i);

                    User user = new User();
                    user.setId(Long.valueOf(userObject.getString("gid")));
                    user.setName(userObject.getString("name"));

                    // Check if email exists
                    if (!userObject.isNull("email")) user.setEmail(userObject.getString("email"));


                    //compare email to the everhouruser list and add the everhour id to the user
                    for (EverhourUser everhourUser : everhourUserList) {
                        if (everhourUser.getEmail().equals(user.getEmail())) {
                            user.setEverhourUserId(everhourUser.getEverhourUserId());
                        }
                    }

                    User newUser = userRepository.getUserById(user.getId());

                    if (newUser == null) {
                        userRepository.save(user);
                    } else {
                        newUser.setName(user.getName());
                        newUser.setEmail(user.getEmail());
                        newUser.setEverhourUserId(user.getEverhourUserId());
                        userRepository.save(newUser);
                    }
                }
            }
        } catch (JSONException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<EverhourUser> getEverhourUserId() {
        try {
            List<EverhourUser> userList = new ArrayList<>();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.everhour.com/team/users"))
                    .header("accept", "application/json")
                    .header("X-Api-Key", everhourApiKey)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JSONArray jsonArray = new JSONArray(response.body());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                Long id = Long.valueOf(jsonObject.getString("id"));
                String email = jsonObject.getString("email");

                userList.add(new EverhourUser(id, email));
            }

            return userList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
