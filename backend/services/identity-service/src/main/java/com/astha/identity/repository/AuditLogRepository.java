package com.astha.identity.repository;

import com.astha.identity.domain.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntry, String> {
    List<AuditLogEntry> findAllByOrderByTimestampDesc();
    List<AuditLogEntry> findByActorIdOrderByTimestampDesc(String actorId);
}
