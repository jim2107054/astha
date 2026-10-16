package com.astha.cases.repository;

import com.astha.cases.domain.CaseAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseActionRepository extends JpaRepository<CaseAction, String> {
    List<CaseAction> findByCaseIdOrderByTimestampAsc(String caseId);
}
