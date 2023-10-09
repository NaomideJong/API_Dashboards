package com.ctsgroup.nl.dashretry.repositories;

import com.ctsgroup.nl.dashretry.models.Activity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends BaseRepository<Activity, Long, JpaSpecificationExecutor<Activity>> {
    List<Activity> findByProjectIdAndTimestampBetween(Long projectId, LocalDateTime startTime, LocalDateTime endTime);
}
