package com.ctsgroup.nl.dashretry.services;

import com.ctsgroup.nl.dashretry.models.Tag;
import com.ctsgroup.nl.dashretry.models.User;
import com.ctsgroup.nl.dashretry.repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public void updateTags() {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://app.asana.com/api/1.0/tags?workspace=12840025534543&opt_fields=name"))
                    .header("accept", "application/json")
                    .header("authorization", "Bearer 1/1205404456046809:6ea5d130ee9bba046f81c789665424d4")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject jsonObject = new JSONObject(response.body());
            JSONArray dataArray = jsonObject.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject userObject = dataArray.getJSONObject(i);

                Tag tag = new Tag();
                tag.setId(Long.valueOf(userObject.getString("gid")));
                tag.setTagName(userObject.getString("name"));

                //check if tag exists
                Tag newTag = tagRepository.getTagById(tag.getId());

                if (newTag == null) {
                    tagRepository.save(tag);
                } else {
                    newTag.setTagName(tag.getTagName());
                    tagRepository.save(newTag);
                }
            }

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }



//        JSONObject jsonObject = new JSONObject(response.body());
//        JSONArray dataArray = jsonObject.getJSONArray("data");
//
//        for (int i = 0; i < dataArray.length(); i++) {
//            JSONObject userObject = dataArray.getJSONObject(i);
//
//            User user = new User();
//            user.setId(Long.valueOf(userObject.getString("gid")));
//            user.setName(userObject.getString("name"));
//
//            // Check if email exists
//            if (!userObject.isNull("email")) {
//                user.setEmail(userObject.getString("email"));
//            }
//
//            User newUser = userRepository.getUserById(user.getId());
//
//            if (newUser == null) {
//                userRepository.save(user);
//            } else {
//                newUser.setName(user.getName());
//                newUser.setEmail(user.getEmail());
//                userRepository.save(newUser);
//            }
//        }
    }
}
