package com.astha.risk.repository;

import com.astha.risk.domain.ListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListEntityRepository extends JpaRepository<ListEntity, UUID> {
    Optional<ListEntity> findByListTypeAndEntityTypeAndEntityValue(String listType, String entityType, String entityValue);

    List<ListEntity> findByListTypeAndExpiresAtAfterOrExpiresAtIsNull(String listType, Instant now);
}
