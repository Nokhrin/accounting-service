package com.nokhrin.accounting.application;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.event.EventRepository;
import com.nokhrin.accounting.domain.event.FundsDeposited;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EventRepository eventRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, eventRepository);
    }

    @Test
    void deposit_validAccount_accountDepositedEventRecorded() {
        UUID accountId = UUID.randomUUID();
        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "Account Service tester");
        Account initialAccount = Account.open(BigDecimal.TEN, holder);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(initialAccount));

        accountService.deposit(accountId, new BigDecimal("90"));

        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).update(accountArgumentCaptor.capture());
        assertEquals(new BigDecimal("100"), accountArgumentCaptor.getValue().balance());

        ArgumentCaptor<FundsDeposited> fundsDepositedArgumentCaptor = ArgumentCaptor.forClass(FundsDeposited.class);
        verify(eventRepository).record(fundsDepositedArgumentCaptor.capture());

        FundsDeposited fundsDepositedRecorded = fundsDepositedArgumentCaptor.getValue();
        assertAll(
                () -> assertEquals(accountId, fundsDepositedRecorded.targetAccountId()),
                () -> assertEquals(new BigDecimal("90"), fundsDepositedRecorded.amount())
        );
    }

}