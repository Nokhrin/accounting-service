package com.nokhrin.accounting.infrastructure.persistence;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JdbcAccountRepositoryTest {
    private static HikariDataSource dataSource;
    private AccountRepository repository;

    @BeforeAll
    static void setUpDb() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5433/testdb");
        config.setUsername("test");
        config.setPassword("test");
        dataSource = new HikariDataSource(config);

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    @BeforeEach
    void setUp(){
        repository=new JdbcAccountRepository(dataSource);
    }

    @AfterAll
    static void tearDown(){
        dataSource.close();
    }

    @Test
    void save_and_findById_cycle_preservesData() {
        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "Test Holder");
        Account accountToSave = Account.create(new BigDecimal("1500.00"), holder);

        repository.save(accountToSave);
        Optional<Account> foundAccountOpt = repository.findById(accountToSave.id());
        assertTrue(foundAccountOpt.isPresent(), "Запись должна быть найдена в БД");
        Account foundAccount = foundAccountOpt.get();

        assertAll(
                () -> assertEquals(accountToSave.id(), foundAccount.id()),
                () -> assertEquals(new BigDecimal("1500.00"), foundAccount.balance()),
                () -> assertEquals(holder.id(), foundAccount.holder().id()),
                () -> assertEquals("Test Holder", foundAccount.holder().displayName())
        );
    }
}