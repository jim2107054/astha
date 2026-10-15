package com.astha.risk.repository;

import com.astha.risk.domain.RuleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleVersionRepository extends JpaRepository<RuleVersion, String> {
    List<RuleVersion> findByRuleIdOrderByVersionNumberDesc(String ruleId);
}
