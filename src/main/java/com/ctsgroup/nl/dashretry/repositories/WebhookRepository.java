package com.ctsgroup.nl.dashretry.repositories;


import com.ctsgroup.nl.dashretry.models.Webhook;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookRepository extends BaseRepository<Webhook, Long, JpaSpecificationExecutor<Webhook>> {

    Webhook getWebhookById(Long id);
}
