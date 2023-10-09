package com.ctsgroup.nl.dashretry.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Entity
@Builder
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "action")
    private String action;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_name")
    private String resourceName;

    @Column(name = "parent_type")
    private String parentType;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "task_weight")
    private int taskWeight;
}
