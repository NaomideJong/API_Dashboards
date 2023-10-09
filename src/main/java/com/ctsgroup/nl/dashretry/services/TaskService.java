package com.ctsgroup.nl.dashretry.services;

import com.ctsgroup.nl.dashretry.models.Task;
import com.ctsgroup.nl.dashretry.repositories.ProjectRepository;
import com.ctsgroup.nl.dashretry.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public void updateTasks() {
        try {
            //loop through all projects
            projectRepository.findAll().forEach(project -> {
                //get all tasks for each project
                updateTasksByProjectId(project.getId().toString());
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateTasksByProjectId(String projectId) {
        try {
            int limit = 100;  // Set your desired limit
            String nextPageToken = null;
            do {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://app.asana.com/api/1.0/tasks?project=" + projectId
                                + "&limit=" + limit
                                + (nextPageToken != null ? "&offset=" + nextPageToken : "")
                                + "&opt_fields=name,completed,custom_fields.display_value,custom_fields.name,projects,modified_at,permalink_url"))
                        .header("accept", "application/json")
                        .header("authorization", "Bearer 1/1205404456046809:6ea5d130ee9bba046f81c789665424d4")
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

                JSONObject jsonObject = new JSONObject(response.body());

                if (jsonObject.has("data")) {
                    JSONArray dataArray = jsonObject.getJSONArray("data");

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject taskObject = dataArray.getJSONObject(i);

                        Task task = new Task();

                        // Check if "name" key exists before retrieving its value
                        String name = taskObject.has("name") ? taskObject.getString("name") : "No name";

                        //Make sure name is not over database limit
                        if (name.length() > 255) {
                            name = name.substring(0, 255);
                        }
                        task.setTaskName(name);

                        task.setId(Long.valueOf(taskObject.getString("gid")));
                        task.setCompleted(taskObject.getBoolean("completed"));
                        task.setUrl(taskObject.getString("permalink_url"));
                        task.setModifiedAt(convertTimeZone(taskObject.getString("modified_at")));

                        //check if task exists
                        Task newTask = taskRepository.getTaskById(task.getId());

                        try {
                            saveTask(task, newTask);
                        } catch (Exception e) {
                            task.setTaskName("No name: Task name invalid");
                            saveTask(task, newTask);
                        }
                    }
                }
                // Check if there's a next page token
                if (jsonObject.has("next_page")) {
                    Object nextPageValue = jsonObject.get("next_page");
                    if (nextPageValue instanceof JSONObject) {
                        nextPageToken = ((JSONObject) nextPageValue).getString("offset");
                    } else {
                        // Handle the case where "next_page" is not a JSONObject
                        nextPageToken = null;
                    }
                } else {
                    nextPageToken = null;
                }

            } while (nextPageToken != null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveTask(Task task, Task newTask) {
        if (newTask == null) {
            taskRepository.save(task);
        } else {
            newTask.setTaskName(task.getTaskName());
            newTask.setCompleted(task.getCompleted());
            newTask.setModifiedAt(task.getModifiedAt());
            newTask.setUrl(task.getUrl());
            taskRepository.save(newTask);
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
