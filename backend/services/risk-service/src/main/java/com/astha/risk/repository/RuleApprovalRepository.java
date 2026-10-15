package com.astha.risk.repository;

import com.astha.risk.domain.RuleApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RuleApprovalRepository extends JpaRepository<RuleApproval, UUID> {
    List<RuleApproval> findByVersionIdOrderByDecidedAtDesc(String versionId);
}
