package com.nokhrin.accounting.application;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.account.AccountStatus;
import com.nokhrin.accounting.domain.event.AccountClosed;
import com.nokhrin.accounting.domain.event.AccountOpened;
import com.nokhrin.accounting.domain.event.EventRepository;
import com.nokhrin.accounting.domain.event.FundsDeposited;
import com.nokhrin.accounting.domain.operation.DuplicateOperationException;
import com.nokhrin.accounting.domain.operation.OperationRegistry;
import com.nokhrin.accounting.domain.operation.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OperationRegistry operationRegistry;

    private AccountService accountService;
    private final AccountHolder holder = new AccountHolder(UUID.randomUUID(), "Test User");

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, eventRepository, operationRegistry);
    }

    @Test
    void deposit_validAccount_accountDepositedEventRecorded() {
        UUID accountId = UUID.randomUUID();
        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "AccountResponse Service tester");
        Account initialAccount = Account.open(BigDecimal.TEN, holder);

        when(accountRepository.findByIdForUpdate(accountId))
                .thenReturn(Optional.of(initialAccount));

        UUID operationId = UUID.randomUUID();

        accountService.deposit(accountId, new BigDecimal("90"), operationId);

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

    @Test
    void openAccount_knownOperationId_throwsAndDoesNotCreate() {
        UUID operationId = UUID.randomUUID();
        doThrow(new DuplicateOperationException(operationId))
                .when(operationRegistry).register(eq(operationId), any(), eq(OperationType.OPEN_ACCOUNT));

        assertThrows(DuplicateOperationException.class,
                () -> accountService.openAccount(BigDecimal.TEN, holder, operationId));

        verify(accountRepository, never()).create(any());
        verify(eventRepository, never()).record(any());
    }

    @Test
    void openAccount_newOperationId_registersOperation() {
        UUID operationId = UUID.randomUUID();

        accountService.openAccount(BigDecimal.TEN, holder, operationId);

        verify(operationRegistry).register(eq(operationId), any(UUID.class), eq(OperationType.OPEN_ACCOUNT));
        verify(accountRepository).create(any());
        verify(eventRepository).record(any(AccountOpened.class));
    }

    @Test
    void closeAccount_activeAccountWithZeroBalance_updatesAccountAndRecordsEvent() {
        UUID accountId = UUID.randomUUID();

        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "Close test holder");

        Account activeAccount = new Account(
                accountId,
                BigDecimal.ZERO,
                AccountStatus.ACTIVE,
                holder
        );

        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(activeAccount));

        accountService.closeAccount(accountId);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).update(accountCaptor.capture());

        Account updatedAccount = accountCaptor.getValue();

        ArgumentCaptor<AccountClosed> eventCaptor = ArgumentCaptor.forClass(AccountClosed.class);
        verify(eventRepository).record(eventCaptor.capture());

        AccountClosed event = eventCaptor.getValue();

        assertAll(
                () -> assertEquals(activeAccount.id(), updatedAccount.id()),
                () -> assertEquals(AccountStatus.CLOSED, updatedAccount.status()),
                () -> assertEquals(0, updatedAccount.balance().compareTo(BigDecimal.ZERO)),
                () -> assertEquals(activeAccount.id(), event.accountId())
        );
    }

}