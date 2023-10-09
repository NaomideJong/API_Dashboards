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
@Table(name = "ProjectTimes")
public class ProjectTime {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "time")
    private int time; //time.total, in seconds

}
