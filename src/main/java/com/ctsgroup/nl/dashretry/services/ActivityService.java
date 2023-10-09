package com.ctsgroup.nl.dashretry.services;

import com.ctsgroup.nl.dashretry.models.Activity;
import com.ctsgroup.nl.dashretry.models.SyncToken;
import com.ctsgroup.nl.dashretry.repositories.ActivityRepository;
import com.ctsgroup.nl.dashretry.repositories.ProjectRepository;
import com.ctsgroup.nl.dashretry.repositories.SyncTokenRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ActivityService {

    Dotenv dotenv = Dotenv.configure().load();
    String apiKey = dotenv.get("ASANA_API_KEY");
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private SyncTokenRepository syncTokenRepository;

    public void addActivities() {
        try {

            //loop through all projects
            projectRepository.findAll().forEach(project -> {
                //get all tasks for each project
                updateActivityByProjectId(project.getId().toString());
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateActivityByProjectId(String projectId) {
        try {
            //get synctoken
            Optional<SyncToken> syncTokenObject = syncTokenRepository.findById(Long.valueOf(projectId));
            String syncToken = syncTokenObject.map(SyncToken::getSyncToken).orElse(null);
            boolean hasMore = false;
            do {
                String encodedSyncToken = "";
                if (syncToken != null) {
                    encodedSyncToken = URLEncoder.encode(syncToken, StandardCharsets.UTF_8);
                }

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://app.asana.com/api/1.0/events?opt_fields=user,parent,created_at,type,resource,action,resource.name&" +
                                "resource=" + projectId +
                                "&sync=" + encodedSyncToken +
                                "&opt_pretty=true"))
                        .header("accept", "application/json")
                        .header("authorization", "Bearer " + apiKey)
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

                JSONObject jsonObject = new JSONObject(response.body());

                // Extract sync and has_more values
                syncToken = jsonObject.has("sync") ? jsonObject.getString("sync") : null;
                if (jsonObject.has("has_more")) hasMore = jsonObject.getBoolean("has_more");

                if (jsonObject.has("data")) {
                    JSONArray dataArray = jsonObject.getJSONArray("data");

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject activityObject = dataArray.getJSONObject(i);

                        if (!Objects.equals(activityObject.getString("type"), "story")) {

                            Activity activity = new Activity();

                            // Check if there is a user before retrieving its value
                            String userId;
                            if (activityObject.has("user") && activityObject.get("user") instanceof JSONObject) {
                                userId = activityObject.getJSONObject("user").optString("gid", "0");
                            } else {
                                userId = "0";
                            }


                            activity.setUserId(userId);

                            activity.setTimestamp(convertTimeZone(activityObject.getString("created_at")));

                            activity.setAction(activityObject.getString("action"));
                            activity.setResourceType(activityObject.getString("type"));

                            //check if resource name exists
                            activity.setResourceName(activityObject.has("resource")
                                    ? activityObject.getJSONObject("resource").optString("name", "No resource name")
                                    : "No resource name");

                            //check if parent exists
                            activity.setParentType(activityObject.has("parent")
                                    && activityObject.get("parent") instanceof JSONObject
                                    ? activityObject.getJSONObject("parent").optString("resource_type", "No parent")
                                    : "No parent");


                            activity.setProjectId(Long.valueOf(projectId));
                            activity.setTaskWeight(TaskWeightCalculation(activity));

                            if (!isDuplicateActivity(activity)) activityRepository.save(activity);
                        }
                    }
                }
                if (jsonObject.has("errors")) {
                    if (jsonObject.getJSONArray("errors").getJSONObject(0).getString("message").equals("Sync token invalid or too old. If you are attempting to keep resources in sync, you must fetch the full dataset for this query now and use the new sync token for the next sync.")) {
                        syncToken = jsonObject.getString("sync");
                        hasMore = true;
                    } else {
                        System.out.println(jsonObject.getJSONArray("errors").getJSONObject(0).getString("message"));
                        return;
                    }
                }
            } while (hasMore);
            SyncToken existingSyncToken = syncTokenRepository.findById(Long.valueOf(projectId)).orElse(null);
            if (existingSyncToken != null) {
                existingSyncToken.setSyncToken(syncToken);
            } else {
                syncTokenRepository.save(new SyncToken(Long.valueOf(projectId), syncToken));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isDuplicateActivity(Activity newActivity) {
        // Find activities for this project within the specified time frame
        LocalDateTime startTime = newActivity.getTimestamp().minusSeconds(10);
        LocalDateTime endTime = newActivity.getTimestamp();

        List<Activity> activitiesWithinTimeFrame = activityRepository
                .findByProjectIdAndTimestampBetween(newActivity.getProjectId(), startTime, endTime);

        // Iterate through the found activities and check for duplicates
        for (Activity activity : activitiesWithinTimeFrame) {
            if (Objects.equals(newActivity.getUserId(), activity.getUserId())
                    && Objects.equals(newActivity.getAction(), activity.getAction())
                    && Objects.equals(newActivity.getResourceType(), activity.getResourceType())
                    && Objects.equals(newActivity.getResourceName(), activity.getResourceName())
                    && Objects.equals(newActivity.getParentType(), activity.getParentType())) {
                // Duplicate found within the time frame, return true
                return true;
            }
        }

        // No duplicate found within the time frame
        return false;
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

    private int TaskWeightCalculation(Activity activity) {
        int points = 0;
        String action = activity.getAction();
        String resourceType = activity.getResourceType();
        String parentType = activity.getParentType();

        // Calculate points based on userId
        if (!activity.getUserId().equals("0")) {
            points += 1;
        }

        // Calculate points based on action
        if ("added".equals(action)) {
            points += 3;
        } else if ("changed".equals(action)) {
            points += 2;
        } else if ("removed".equals(action)) {
            points += 1;
        }

        // Calculate points based on resourceType
        if ("task".equals(resourceType)) {
            points += 2;
        } else if ("project".equals(resourceType)) {
            points += 4;
        } else if ("attachment".equals(resourceType)) {
            points += 2;
        }

        // Calculate points based on resourceName
        if (!activity.getResourceName().equals("No resource name")) {
            points += 1;
        }

        // Calculate points based on parentType
        if ("task".equals(parentType)) {
            points += 1;
        } else if ("section".equals(parentType)) {
            points += 1;
        } else if ("project".equals(parentType)) {
            points += 8;
        } else if ("tag".equals(parentType)) {
            points += 6;
        } else if ("portfolio".equals(parentType)) {
            points += 1;
        }

        //converting point system to minutes
        return points * 60;
    }
}




