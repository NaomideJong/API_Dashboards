package com.ctsgroup.nl.dashretry.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Entity
@Builder
@Table(name = "project_times")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "date")
    @JsonProperty("date")
    private LocalDate date;

    @Column(name = "time")
    private int time; //in seconds

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "task_name")
    private String taskName;

}
