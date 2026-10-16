package com.astha.disputes.repository;

import com.astha.disputes.domain.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, String> {
    Optional<Dispute> findByTxnId(String txnId);
    List<Dispute> findAllByOrderByDeadlineUtcAsc();
}
