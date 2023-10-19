package com.ctsgroup.nl.dashretry.repositories;

import com.ctsgroup.nl.dashretry.models.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends BaseRepository<User, Long, JpaSpecificationExecutor<User>> {
    User getUserById(Long id);

    User findByEverhourUserId(Long everhourUserId);

    List<User> findByEverhourUserIdIsNotNull();
}
