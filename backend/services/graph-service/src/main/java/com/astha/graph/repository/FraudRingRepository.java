package com.astha.graph.repository;

import com.astha.graph.domain.FraudRing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FraudRingRepository extends JpaRepository<FraudRing, String> {
    List<FraudRing> findByStatus(String status);
}
