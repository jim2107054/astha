package com.astha.analytics.repository;

import com.astha.analytics.domain.RuleQualityStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleQualityRepository extends JpaRepository<RuleQualityStat, String> {
}
