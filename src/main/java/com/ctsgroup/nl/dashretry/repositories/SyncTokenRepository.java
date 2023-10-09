package com.ctsgroup.nl.dashretry.repositories;

import com.ctsgroup.nl.dashretry.models.SyncToken;
import com.ctsgroup.nl.dashretry.models.Task;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncTokenRepository extends BaseRepository<SyncToken, Long, JpaSpecificationExecutor<SyncToken>>{

}