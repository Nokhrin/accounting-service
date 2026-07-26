package com.nokhrin.accounting.application;

import net.jqwik.api.*;
import net.jqwik.api.constraints.Positive;
import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.event.Event;
import com.nokhrin.accounting.domain.event.EventRepository;
import com.nokhrin.accounting.domain.event.FundsDeposited;
import com.nokhrin.accounting.domain.operation.DepositResult;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AccountServicePropTest {
    @Provide
    Arbitrary<BigDecimal> nonNegativeBigDecimal(){
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
            return Optional.of(accountMap.get(accountId));
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

    @Property
    void deposit_balanceIncreasedByAmount(
            @ForAll @From("nonNegativeBigDecimal") BigDecimal initialBalance,
            @ForAll @Positive BigDecimal depositAmount
    ) {
        InMemoryAccountRepository accountRepository = new InMemoryAccountRepository();
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        AccountService accountService = new AccountService(accountRepository, eventRepository);

        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "Props tester");
        Account account = Account.open(initialBalance, holder);
        accountRepository.create(account);

        DepositResult result = accountService.deposit(account.id(), depositAmount);
        assertAll(
                () -> assertEquals(1, eventRepository.eventList.size()),
                () -> assertEquals(initialBalance.add(depositAmount), result.accountAfter().balance()),
                () -> assertInstanceOf(FundsDeposited.class, eventRepository.eventList.getFirst()),
                () -> assertEquals(depositAmount, ((FundsDeposited) eventRepository.eventList.getFirst()).amount())
        );

    }
}