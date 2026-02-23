package com.example.Account.Service.repository;

import com.example.Account.Service.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Account> findByUserIdAndStatus(UUID userId, Account.AccountStatus status);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.userId = :userId AND a.status = 'ACTIVE'")
    long countActiveAccountsByUserId(@Param("userId") UUID userId);

    boolean existsByAccountNumber(String accountNumber);
}
