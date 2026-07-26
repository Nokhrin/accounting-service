package com.nokhrin.accounting.infrastructure.persistence;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.account.AccountStatus;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    void create_newAccount_savedToRepo() throws SQLException {
        AccountHolder holder=new AccountHolder(UUID.randomUUID(), "account creator");
        Account accountToCreate = Account.open(BigDecimal.ZERO, holder);
        repository.create(accountToCreate);

        try (Connection connection=dataSource.getConnection();
             PreparedStatement preparedStatement=connection.prepareStatement(
                    """
                SELECT id, balance, status, holder_id, holder_display_name
                FROM public.accounts where id = ?;
                """
             )){
                 preparedStatement.setObject(1,accountToCreate.id());
                 try (ResultSet resultSet=preparedStatement.executeQuery()){
        assertAll(
                ()->assertEquals(accountToCreate.id(), resultSet.getObject("id", UUID.class)),
                ()->assertEquals(BigDecimal.ZERO, resultSet.getBigDecimal("balance")),
                ()->assertEquals(AccountStatus.ACTIVE, resultSet.getObject("status", AccountStatus.class)),
                ()->assertEquals(holder.id(), resultSet.getObject("holder_id", UUID.class))
        );
                 }
        }


    }

    @Test
    void update_existingAccount_updatedBalanceAndStatus() throws SQLException {
        AccountHolder holder=new AccountHolder(UUID.randomUUID(), "account updater");
        Account accountToUpdate = Account.open(BigDecimal.ONE,holder);
        repository.create(accountToUpdate);
        Account accountUpdatedData = new Account(
                accountToUpdate.id(),
                BigDecimal.ZERO,
                AccountStatus.CLOSED,
                holder);
        repository.update(accountUpdatedData);
        Optional<Account> accountUpdatedOpt = repository.findById(accountUpdatedData.id());
        Account accountUpdated=accountUpdatedOpt.get();

        assertAll(
                ()->assertEquals(accountToUpdate.id(), accountUpdated.id()),
                ()->assertEquals(BigDecimal.ZERO, accountUpdated.balance()),
                ()->assertEquals(AccountStatus.CLOSED, accountUpdated.status()),
                ()->assertEquals(holder, accountUpdated.holder())
        );
    }

    @Test
    void update_nonExistingAccount_throwsExc(){
        AccountHolder holder=new AccountHolder(UUID.randomUUID(), "non existing");
        Account accountNonExisting = new Account(
                UUID.randomUUID(),
                new BigDecimal("1000"),
                AccountStatus.ACTIVE,
                holder
        );

        assertThrows(RuntimeException.class,
                ()->repository.update(accountNonExisting));
    }

    @Test
    void findById_nonExistingAcc_returnsEmpty() throws SQLException {
        Optional<Account> accountOptional = repository.findById(UUID.randomUUID());
        assertTrue(accountOptional.isEmpty());
    }
}