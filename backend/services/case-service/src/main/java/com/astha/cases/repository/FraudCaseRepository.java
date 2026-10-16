package com.astha.cases.repository;

import com.astha.cases.domain.FraudCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudCaseRepository extends JpaRepository<FraudCase, String> {
    Optional<FraudCase> findByEntityIdAndStatusNot(String entityId, String status);
    List<FraudCase> findAllByOrderByTriageScoreDesc();
}
