package com.ctsgroup.nl.dashretry.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, ID, J> extends JpaRepository<T, ID> {
}
