package com.astha.providermock.repository;

import com.astha.providermock.domain.MockAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MockAccountRepository extends JpaRepository<MockAccount, String> {
    Optional<MockAccount> findByMsisdn(String msisdn);
}
