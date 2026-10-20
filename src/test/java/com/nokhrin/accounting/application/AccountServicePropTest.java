package com.nokhrin.accounting.application;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.event.Event;
import com.nokhrin.accounting.domain.event.EventRepository;
import com.nokhrin.accounting.domain.event.FundsDeposited;
import com.nokhrin.accounting.domain.operation.DepositResult;
import com.nokhrin.accounting.domain.operation.OperationRegistry;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.lifecycle.BeforeTry;

import java.math.BigDecimal;
import java.util.*;

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

    private static class InMemoryOperationRegistry implements OperationRegistry {
        private final Map<UUID, String> operationsRegistered = new HashMap<>();

        @Override
        public boolean exists(UUID operationId) {
            return operationsRegistered.containsKey(operationId);
        }

        @Override
        public void register(UUID operationId, UUID accountId, String operationType) {
            operationsRegistered.put(operationId, operationType);
        }
    }

    @BeforeTry
    void setUp() {
        accountRepository = new InMemoryAccountRepository();
        eventRepository = new InMemoryEventRepository();
        operationRegistry = new InMemoryOperationRegistry();
        accountService = new AccountService(accountRepository, eventRepository, operationRegistry);
    }

    @Property
    void deposit_balanceIncreasedByAmount(
            @ForAll @From("nonNegativeBigDecimal") BigDecimal initialBalance,
            @ForAll @Positive BigDecimal depositAmount
    ) {

        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "Props tester");
        Account account = Account.open(initialBalance, holder);
        accountRepository.create(account);

        DepositResult result = accountService.deposit(account.id(), depositAmount);
        assertAll(
                () -> assertEquals(1, eventRepository.eventList.size()),
                () -> assertEquals(initialBalance.add(depositAmount), result.accountAfter().balance()),
                () -> assertInstanceOf(FundsDeposited.class, eventRepository.eventList.getFirst()),
                () -> {
                    Event depositEvent = eventRepository.eventList.getFirst();
                    if (depositEvent instanceof FundsDeposited deposited) {
                        assertEquals(depositAmount, deposited.amount());
                    }
                }
        );

    }
}