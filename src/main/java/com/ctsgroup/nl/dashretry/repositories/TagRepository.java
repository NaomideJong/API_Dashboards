package com.ctsgroup.nl.dashretry.repositories;

import com.ctsgroup.nl.dashretry.models.Tag;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends BaseRepository<Tag, Long, JpaSpecificationExecutor<Tag>> {
    Tag getTagById(Long id);
}
