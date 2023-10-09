package com.ctsgroup.nl.dashretry.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Entity
@Builder
@Table(name = "projects")
public class Project {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed")
    private Boolean completed;

    @Column(name = "url")
    private String url;

}
