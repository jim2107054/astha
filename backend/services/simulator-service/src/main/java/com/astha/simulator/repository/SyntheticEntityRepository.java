package com.astha.simulator.repository;

import com.astha.simulator.domain.SyntheticEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyntheticEntityRepository extends JpaRepository<SyntheticEntity, String> {
    List<SyntheticEntity> findByEntityType(String entityType);
}
