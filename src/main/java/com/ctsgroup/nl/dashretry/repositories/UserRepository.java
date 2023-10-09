package com.ctsgroup.nl.dashretry.repositories;

import com.ctsgroup.nl.dashretry.models.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseRepository<User, Long, JpaSpecificationExecutor<User>>{
        User getUserById(Long id);
}
