package com.nokhrin.accounting.application;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.account.AccountStatus;
import com.nokhrin.accounting.domain.event.Event;
import com.nokhrin.accounting.domain.event.EventRepository;
import com.nokhrin.accounting.domain.operation.OperationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@Testcontainers
class AccountServiceTransactionalTest {
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
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @MockitoBean
    private EventRepository eventRepository;
    @Autowired
    private OperationRegistry operationRegistry;

    @Test
    void deposit_eventFailureRollsBackAccount() {
        Account account = Account.open(BigDecimal.TEN, new AccountHolder(UUID.randomUUID(), "h"));
        accountRepository.create(account);
        BigDecimal balanceBefore = accountRepository.findById(account.id()).orElseThrow().balance();

        UUID operationId = UUID.randomUUID();

        doThrow(new RuntimeException("DB failure"))
                .when(eventRepository).record(any(Event.class));

        assertThrows(RuntimeException.class,
                () -> accountService.deposit(account.id(), BigDecimal.TEN, operationId));

        BigDecimal balanceAfter = accountRepository.findById(account.id()).orElseThrow().balance();
        assertEquals(0, balanceBefore.compareTo(balanceAfter),
                "Account balance should be unchanged after failed transaction");
    }

    @Test
    void closeAccount_eventFailureRollsBackOperation() {
        Account account = Account.open(BigDecimal.ZERO, new AccountHolder(UUID.randomUUID(), "holder name"));
        accountRepository.create(account);

        doThrow(new RuntimeException("Error on recording event"))
                .doNothing()
                .when(eventRepository).record(any(Event.class));

        assertThrows(RuntimeException.class, () -> accountService.closeAccount(account.id()));

        Account afterFailure = accountRepository.findById(account.id()).orElseThrow();
        assertEquals(AccountStatus.ACTIVE, afterFailure.status());

        Account closed = accountService.closeAccount(account.id());
        assertEquals(AccountStatus.CLOSED, closed.status());
    }
}