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
@Table(name = "tasks")
public class Task {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @Column(name = "completed")
    private Boolean completed;

    @Column(name = "url")
    private String url;

}
