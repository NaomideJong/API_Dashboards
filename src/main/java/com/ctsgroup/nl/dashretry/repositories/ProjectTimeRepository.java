package com.ctsgroup.nl.dashretry.repositories;

import com.ctsgroup.nl.dashretry.models.ProjectTime;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ProjectTimeRepository extends BaseRepository<ProjectTime, Long, JpaSpecificationExecutor<ProjectTime>>{
    Optional<ProjectTime> findTopByProjectIdAndDateAndTaskId(Long projectId, LocalDate date, String taskId);
}
