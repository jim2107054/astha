package com.astha.providermock.repository;

import com.astha.providermock.domain.MockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MockTransactionRepository extends JpaRepository<MockTransaction, String> {
    Optional<MockTransaction> findByIdempotencyKey(String idempotencyKey);
}
