package com.ctsgroup.nl.dashretry.repositories;

import com.ctsgroup.nl.dashretry.models.UserTime;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserTimeRepository extends BaseRepository<UserTime, Long, JpaSpecificationExecutor<UserTime>> {
    Optional<UserTime> findTopByUserIdAndDateAndTaskId(Long userId, LocalDate date, String taskId);
}
