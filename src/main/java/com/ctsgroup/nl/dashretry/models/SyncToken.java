package com.ctsgroup.nl.dashretry.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Entity
@Builder
@Table(name = "sync_tokens")
public class SyncToken {

    @Id
    @Column(name = "project_id", nullable = false, unique = true)
    private Long projectId;

    @Column(name = "sync_token")
    private String syncToken;
}
