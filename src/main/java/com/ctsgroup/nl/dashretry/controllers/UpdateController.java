package com.ctsgroup.nl.dashretry.controllers;

import com.ctsgroup.nl.dashretry.services.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/update")
public class UpdateController {

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ActivityService activityService;

    @GetMapping
    public String getWebhook() {
        userService.updateUsers();
        projectService.updateProjects();
        activityService.addActivities();
        return webhookService.getWebhook();
    }

}
