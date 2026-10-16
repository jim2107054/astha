package com.astha.cases.repository;

import com.astha.cases.domain.CaseAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseAlertRepository extends JpaRepository<CaseAlert, String> {
    List<CaseAlert> findByCaseId(String caseId);
}
