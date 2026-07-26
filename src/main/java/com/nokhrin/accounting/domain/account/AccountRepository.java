package com.nokhrin.accounting.domain.account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    void create(Account account);
    void update(Account account);
    Optional<Account> findById(UUID accountId);
}
