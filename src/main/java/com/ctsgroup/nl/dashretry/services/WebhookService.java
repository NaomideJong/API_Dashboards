package com.ctsgroup.nl.dashretry.services;

import com.ctsgroup.nl.dashretry.models.Activity;
import com.ctsgroup.nl.dashretry.models.Webhook;
import com.ctsgroup.nl.dashretry.repositories.WebhookRepository;
import com.ctsgroup.nl.dashretry.repositories.ActivityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

@Service
public class WebhookService {

    @Autowired
    private WebhookRepository webhookRepository;

    @Autowired
    private ActivityRepository activityRepository;

    public String getWebhook() {
        try {
            LocalDateTime timestamp = LocalDateTime.now();
            Long id = 1L;
            Webhook hook = webhookRepository.getWebhookById(id);
            hook.setTimestamp(timestamp.toString());
            webhookRepository.save(hook);
            return "Success";
        } catch (Exception e) {
            System.out.println(e);
            return "Error";
        }
    }

//    public String asanaWebhook(String body) {
//        try{
//            ObjectMapper objectMapper = new ObjectMapper();
//            Activity[] asanaActivities = objectMapper.readValue(body, Activity[].class);
//
//            // Process and save the activities to the database
//            for (Activity asanaActivity : asanaActivities) {
//
//                Activity activity = new Activity();
//                activity.setUsername(asanaActivity.getUsername());
//                activity.setResourceType(asanaActivity.getResourceType());
//                activity.setResource(asanaActivity.getResource());
//                activity.setAction(asanaActivity.getAction());
//                activity.setTimestamp(asanaActivity.getTimestamp());
//                activityRepository.save(activity);
//            }
//            return "Success";
//        }
//        catch (Exception e) {
//            System.out.println(e);
//            return "Error";
//        }
//    }


    public String getTaskById(String task_gid, String changedField) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://app.asana.com/api/1.0/tasks/" + task_gid + "?opt_fields=name,modified_at," + changedField))
                    .header("accept", "application/json")
                    .header("authorization", "Bearer 1/1205404456046809:6ea5d130ee9bba046f81c789665424d4")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
        } catch (Exception e) {
            System.out.println(e);
        }
        return task_gid;
    }
}
