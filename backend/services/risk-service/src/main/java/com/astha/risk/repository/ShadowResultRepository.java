package com.astha.risk.repository;

import com.astha.risk.domain.ShadowResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShadowResultRepository extends JpaRepository<ShadowResult, Long> {
    List<ShadowResult> findByCandidateVersionId(String candidateVersionId);
}
