package com.nokhrin.accounting.domain.account;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    void create(Account account) throws SQLException;
    void update(Account account) throws SQLException;
    Optional<Account> findById(UUID id) throws SQLException;
}
