package com.ctsgroup.nl.dashretry.controllers;

import com.ctsgroup.nl.dashretry.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/update")
@EnableAutoConfiguration
public class UpdateController {

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ActivitiesAndTimesService activityService;

    @GetMapping
    public String getWebhook() {
        projectService.updateProjects();
        userService.updateUsers();
        activityService.updateActivitiesAndTimes();
        return webhookService.getWebhook();
    }

}
