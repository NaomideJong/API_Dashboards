package com.ctsgroup.nl.dashretry.services;

import com.ctsgroup.nl.dashretry.models.User;
import com.ctsgroup.nl.dashretry.repositories.ProjectRepository;
import com.ctsgroup.nl.dashretry.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivitiesAndTimesService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserTimeService userTimeService;

    @Autowired
    private ProjectTimeService projectTimeService;

    public void updateActivitiesAndTimes() {
        updateActivitiesAndProjectTimes();
        updateUserTimes();
    }

    public void updateActivitiesAndProjectTimes(){
        try{//loop through all projects
            projectRepository.findAll().forEach(project -> {
                //get all tasks for each project
                String projetId = project.getId().toString();
                activityService.updateActivityByProjectId(projetId);
                projectTimeService.updateEverhourProjectTime(projetId);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateUserTimes(){
        try{//loop through all users with an everhour user id
            List<User> usersWithEverhourUserId = userRepository.findByEverhourUserIdIsNotNull();
            usersWithEverhourUserId.forEach(user -> {
                userTimeService.updateUserTimeByUser(user.getEverhourUserId().toString());
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
