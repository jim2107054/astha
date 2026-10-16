package com.astha.simulator.repository;

import com.astha.simulator.domain.GroundTruthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroundTruthRepository extends JpaRepository<GroundTruthRecord, String> {
    List<GroundTruthRecord> findByTypology(String typology);
}
