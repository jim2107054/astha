package com.astha.cases.repository;

import com.astha.cases.domain.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, String> {
    List<ApprovalRequest> findByStatus(String status);
}
