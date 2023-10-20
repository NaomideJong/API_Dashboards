package com.ctsgroup.nl.dashretry.services;

import com.ctsgroup.nl.dashretry.models.Webhook;
import com.ctsgroup.nl.dashretry.repositories.ActivityRepository;
import com.ctsgroup.nl.dashretry.repositories.WebhookRepository;
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
}
