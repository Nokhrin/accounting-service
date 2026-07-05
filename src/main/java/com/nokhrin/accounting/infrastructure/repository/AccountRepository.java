package com.nokhrin.accounting.infrastructure.repository;

import com.nokhrin.accounting.domain.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(UUID id);
}
