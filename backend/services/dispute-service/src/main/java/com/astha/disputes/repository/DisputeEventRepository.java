package com.astha.disputes.repository;

import com.astha.disputes.domain.DisputeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeEventRepository extends JpaRepository<DisputeEvent, String> {
    List<DisputeEvent> findByDisputeIdOrderByTimestampAsc(String disputeId);
}
