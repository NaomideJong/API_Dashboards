package com.ctsgroup.nl.dashretry.repositories;

import com.ctsgroup.nl.dashretry.models.Task;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends BaseRepository<Task, Long, JpaSpecificationExecutor<Task>>{
        Task getTaskById(Long id);
}