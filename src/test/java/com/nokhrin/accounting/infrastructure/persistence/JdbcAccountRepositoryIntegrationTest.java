package com.nokhrin.accounting.infrastructure.persistence;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.account.AccountStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
class JdbcAccountRepositoryIntegrationTest {
    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void create_and_findById_dataPersists() throws SQLException {
        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "integration tester");
        Account account = Account.open(BigDecimal.ZERO, holder);

        accountRepository.create(account);

        Optional<Account> accountFoundOpt = accountRepository.findById(account.id());
        Account accountFound = accountFoundOpt.get();
        assertAll(
                ()->assertEquals(account.id(), accountFound.id()),
                ()->assertEquals(0, accountFound.balance().compareTo(BigDecimal.ZERO)),
                ()->assertEquals(AccountStatus.ACTIVE, accountFound.status()),
                ()->assertEquals(holder.id(), accountFound.holder().id())
        );

    }
}