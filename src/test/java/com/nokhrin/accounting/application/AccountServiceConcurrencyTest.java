package com.nokhrin.accounting.application;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class AccountServiceConcurrencyTest {
    @Container
    static org.testcontainers.postgresql.PostgreSQLContainer postgreSQLContainer =
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

    @Test
    void concurrentDeposits_doNotLoseUpdates() throws Exception {
        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "Concurrent");
        Account account = Account.open(BigDecimal.ZERO, holder);
        accountRepository.create(account);

        int threads = 10;
        BigDecimal amount = BigDecimal.ONE;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Object>> futures = IntStream.range(0, threads)
                .mapToObj(_ -> executor.submit(() -> {
                    UUID operationId = UUID.randomUUID();
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    accountService.deposit(account.id(), amount, operationId);
                    return null;
                }))
                .toList();

        start.countDown();
        for (Future<?> f : futures) f.get();
        executor.shutdown();

        Account result = accountRepository.findById(account.id()).orElseThrow();
        assertEquals(0, result.balance().compareTo(new BigDecimal("10.00")),
                "Expected 10.00 but was " + result.balance());
    }
}