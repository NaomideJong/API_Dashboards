package com.ctsgroup.nl.dashretry.repositories;


import com.ctsgroup.nl.dashretry.models.Webhook;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebhookRepository extends BaseRepository<Webhook, Long, JpaSpecificationExecutor<Webhook>> {

        Webhook getWebhookById(Long id);
}
