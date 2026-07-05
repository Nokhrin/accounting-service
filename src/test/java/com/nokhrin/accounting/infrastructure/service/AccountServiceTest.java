package com.nokhrin.accounting.infrastructure.service;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountNotFoundException;
import com.nokhrin.accounting.domain.event.DepositEvent;
import com.nokhrin.accounting.domain.operation.Operation;
import com.nokhrin.accounting.domain.operation.OperationStatus;
import com.nokhrin.accounting.domain.operation.OperationType;
import com.nokhrin.accounting.domain.shared.Actor;
import com.nokhrin.accounting.domain.shared.CustomerActor;
import com.nokhrin.accounting.infrastructure.dto.DepositRequest;
import com.nokhrin.accounting.infrastructure.dto.OperationResponse;
import com.nokhrin.accounting.infrastructure.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AccountService accountService;

    @Test
    void deposit_existingAccount_savesAndPublishesEvent() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.create(new BigDecimal("999.99"));
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocationOnMock -> (Account) invocationOnMock.getArgument(0));

        DepositRequest request = new DepositRequest(new BigDecimal(".01"));
        Actor actor = new CustomerActor("user1");

        OperationResponse response = accountService.deposit(
                accountId,
                request,
                actor
        );

        assertAll(
                ()->assertEquals(new BigDecimal("1000.00"), response.balanceUpdated()),
                ()->assertEquals(OperationType.DEPOSIT, response.operationType()),
                ()->assertEquals(OperationStatus.COMPLETED, response.status()),
                ()->assertEquals(accountId,response.accountId())
        );

        verify(accountRepository).findById(accountId);
        verify(accountRepository).save(any(Account.class));
        verify(eventPublisher).publishEvent(any(DepositEvent.class));
    }

    @Test
    void deposit_existingAccount_capturesEventDetails(){
        UUID accountId = UUID.randomUUID();
        Account accountBefore = Account.create(new BigDecimal("1000"));

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(accountBefore));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocationOnMock -> (Account) invocationOnMock.getArgument(0));

        DepositRequest request = new DepositRequest(new BigDecimal(".01"));
        Actor actor = new CustomerActor("user1");

        accountService.deposit(accountId, request, actor);

        ArgumentCaptor<DepositEvent> depositEventArgumentCaptor = ArgumentCaptor.forClass(DepositEvent.class);
        verify(eventPublisher).publishEvent(depositEventArgumentCaptor.capture());

        DepositEvent capturedEvent = depositEventArgumentCaptor.getValue();
        assertAll(
                ()->assertEquals(accountId, capturedEvent.accountId()),
                ()->assertEquals(new BigDecimal(".01"), capturedEvent.amount()),
                ()->assertEquals(new BigDecimal("1000"), capturedEvent.balanceBefore()),
                ()->assertEquals(new BigDecimal("1000.01"), capturedEvent.balanceAfter()),
                ()->assertEquals("user1", capturedEvent.actor().id())
        );
    }

    @Test
    void deposit_nonExistingAccount_throwsException(){
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        DepositRequest depositRequest = new DepositRequest(BigDecimal.valueOf(1));
        Actor actor = new CustomerActor("user1");

        assertThrows(AccountNotFoundException.class,
                ()-> accountService.deposit(accountId,depositRequest, actor));

        verify(accountRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deposit_validAccount_savedExactlyOnce(){
        UUID accountId = UUID.randomUUID();
        Account account = Account.create(new BigDecimal("1000"));
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        accountService.deposit(
                accountId,
                new DepositRequest(new BigDecimal("1")),
                new CustomerActor("user1")
        );

        verify(accountRepository, times(1)).save(any(Account.class));
        verify(eventPublisher, times(1)).publishEvent(any(DepositEvent.class));
    }
}