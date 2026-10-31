package com.nokhrin.accounting.application;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.event.Event;
import com.nokhrin.accounting.domain.event.EventRepository;
import com.nokhrin.accounting.domain.event.FundsDeposited;
import com.nokhrin.accounting.domain.operation.DepositResult;
import com.nokhrin.accounting.domain.operation.DuplicateOperationException;
import com.nokhrin.accounting.domain.operation.OperationRegistry;
import com.nokhrin.accounting.domain.operation.OperationType;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.lifecycle.BeforeTry;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class AccountServicePropTest {
    InMemoryAccountRepository accountRepository;
    InMemoryEventRepository eventRepository;
    InMemoryOperationRegistry operationRegistry;
    AccountService accountService;

    @Provide
    Arbitrary<BigDecimal> nonNegativeBigDecimal() {
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, new BigDecimal("1000000000000.00"))
                .ofScale(2);
    }

    private static class InMemoryAccountRepository implements AccountRepository {
        private final Map<UUID, Account> accountMap = new HashMap<>();

        @Override
        public void create(Account account) {
            accountMap.put(account.id(), account);
        }

        @Override
        public void update(Account account) {
            accountMap.put(account.id(), account);
        }

        @Override
        public Optional<Account> findById(UUID accountId) {
            return Optional.ofNullable(accountMap.get(accountId));
        }

        @Override
        public Optional<Account> findByIdForUpdate(UUID accountId) {
            return findById(accountId);
        }
    }

    private static class InMemoryEventRepository implements EventRepository {
        private final List<Event> eventList = new ArrayList<>();

        @Override
        public void record(Event event) {
            eventList.add(event);
        }

        @Override
        public List<Event> findByAccountId(UUID accountId) {
            return eventList;
        }
    }


    @BeforeTry
    void setUp() {
        accountRepository = new InMemoryAccountRepository();
        eventRepository = new InMemoryEventRepository();
        operationRegistry = new InMemoryOperationRegistry();
        accountService = new AccountService(accountRepository, eventRepository, operationRegistry);
    }

    private static final AccountHolder HOLDER =
            new AccountHolder(UUID.fromString("00000000-0000-0000-0000-000000000001"), "props_holder");

    private static class InMemoryOperationRegistry implements OperationRegistry {
        private final Map<UUID, OperationType> operationsRegistered = new ConcurrentHashMap<>();

        @Override
        public void register(UUID operationId, UUID accountId, OperationType operationType) {
            if (operationsRegistered.putIfAbsent(operationId, operationType) != null) {
                throw new DuplicateOperationException(operationId);
            }
        }
    }

    @Property
    void deposit_balanceIncreasedByAmount(
            @ForAll @From("nonNegativeBigDecimal") BigDecimal initialBalance,
            @ForAll @Positive BigDecimal depositAmount
    ) {
        Account account = Account.open(initialBalance, HOLDER);
        accountRepository.create(account);
        UUID operationId = UUID.randomUUID();

        DepositResult result = accountService.deposit(account.id(), depositAmount, operationId);

        assertAll(
                () -> assertEquals(1, eventRepository.eventList.size()),
                () -> assertEquals(0, initialBalance.add(depositAmount).compareTo(result.accountAfter().balance())),
                () -> assertInstanceOf(FundsDeposited.class, eventRepository.eventList.getFirst()),
                () -> {
                    Event depositEvent = eventRepository.eventList.getFirst();
                    if (depositEvent instanceof FundsDeposited deposited) {
                        assertEquals(0, depositAmount.compareTo(deposited.amount()));
                    }
                }
        );
    }

    @Property
    void deposit_concurrentSameOperationId_executesOnce(
            @ForAll @From("nonNegativeBigDecimal") BigDecimal initialBalance,
            @ForAll @Positive BigDecimal amount) {
        Account account = Account.open(initialBalance, HOLDER);
        accountRepository.create(account);
        UUID operationId = UUID.randomUUID();

        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = IntStream.range(0, 4)
                .mapToObj(i -> executor.submit(() -> {
                    start.await();
                    try {
                        accountService.deposit(account.id(), amount, operationId);
                        return "ok";
                    } catch (DuplicateOperationException e) {
                        return "duplicate";
                    }
                }))
                .toList();
        start.countDown();
        List<String> results = futures.stream().map(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();
        executor.shutdown();

        assertEquals(1, results.stream().filter("ok"::equals).count());
        assertEquals(3, results.stream().filter("duplicate"::equals).count());
        assertEquals(0, accountRepository.findById(account.id()).orElseThrow().balance()
                .compareTo(initialBalance.add(amount)));
    }
}