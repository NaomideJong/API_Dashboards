package com.ctsgroup.nl.dashretry.repositories;

import com.ctsgroup.nl.dashretry.models.Project;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends BaseRepository<Project, Long, JpaSpecificationExecutor<Project>> {
    Project getProjectById(Long id);
}
