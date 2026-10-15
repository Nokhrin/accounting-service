package com.nokhrin.accounting.infrastructure;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.account.AccountStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class AccountRepositoryTest {
    @Container
    static PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("test-db")
                    .withUsername("test-user")
                    .withPassword("test-password");

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void create_newAccount_savedToRepo() {
        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "integration tester");
        Account account = Account.open(BigDecimal.ZERO, holder);

        accountRepository.create(account);
        Account accountFound = accountRepository.findById(account.id()).orElseThrow();

        assertAll(
                () -> assertEquals(account.id(), accountFound.id()),
                () -> assertEquals(0, accountFound.balance().compareTo(BigDecimal.ZERO)),
                () -> assertEquals(AccountStatus.ACTIVE, accountFound.status()),
                () -> assertEquals(holder.id(), accountFound.holder().id())
        );

    }

    @Test
    void update_existingAccount_updatedBalanceAndStatus() {
        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "account updater");

        Account accountToUpdate = Account.open(BigDecimal.ZERO, holder);
        accountRepository.create(accountToUpdate);

        Account closedAccount = accountToUpdate.close();
        accountRepository.update(closedAccount);

        Optional<Account> accountUpdatedOpt = accountRepository.findById(closedAccount.id());
        Account accountUpdated = accountUpdatedOpt.orElseThrow();

        assertAll(
                () -> assertEquals(accountToUpdate.id(), accountUpdated.id()),
                () -> assertEquals(0, accountUpdated.balance().compareTo(BigDecimal.ZERO)),
                () -> assertEquals(AccountStatus.CLOSED, accountUpdated.status()),
                () -> assertEquals(holder, accountUpdated.holder())
        );
    }

    @Test
    void update_nonExistingAccount_throwsExc() {
        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "non existing");
        Account accountNonExisting = new Account(
                UUID.randomUUID(),
                new BigDecimal("1000"),
                AccountStatus.ACTIVE,
                holder
        );

        assertThrows(IllegalArgumentException.class,
                () -> accountRepository.update(accountNonExisting));
    }

    @Test
    void findById_nonExistingAcc_returnsEmpty() {
        Optional<Account> accountOptional = accountRepository.findById(UUID.randomUUID());
        assertTrue(accountOptional.isEmpty());
    }

    @Test
    void create_withInitialBalance_savedCorrectly() {
        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "holder");
        Account account = Account.open(new BigDecimal("100.50"), holder);

        accountRepository.create(account);

        Account found = accountRepository.findById(account.id()).orElseThrow();
        assertEquals(0, found.balance().compareTo(new BigDecimal("100.50")));
    }

    @Test
    void update_balanceOnly_statusUnchanged() {
        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "holder");
        Account account = Account.open(BigDecimal.TEN, holder);
        accountRepository.create(account);

        Account updatedBalance = new Account(
                account.id(),
                new BigDecimal("50"),
                AccountStatus.ACTIVE,
                holder
        );
        accountRepository.update(updatedBalance);

        Account found = accountRepository.findById(account.id()).orElseThrow();
        assertEquals(0, found.balance().compareTo(new BigDecimal("50")));
        assertEquals(AccountStatus.ACTIVE, found.status());
    }
}