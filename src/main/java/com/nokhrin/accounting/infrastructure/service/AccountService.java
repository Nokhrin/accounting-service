package com.nokhrin.accounting.infrastructure.service;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountNotFoundException;
import com.nokhrin.accounting.domain.event.DepositEvent;
import com.nokhrin.accounting.domain.operation.Deposit;
import com.nokhrin.accounting.domain.operation.OperationStatus;
import com.nokhrin.accounting.domain.operation.OperationType;
import com.nokhrin.accounting.domain.shared.Actor;
import com.nokhrin.accounting.infrastructure.dto.DepositRequest;
import com.nokhrin.accounting.infrastructure.dto.OperationResponse;
import com.nokhrin.accounting.infrastructure.repository.AccountRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AccountService(AccountRepository accountRepository, ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OperationResponse deposit(
            UUID accountId,
            DepositRequest depositRequest,
            Actor actor
    ){
        Account accountBeforeDeposit = accountRepository.findById(accountId)
                .orElseThrow(()->new AccountNotFoundException(accountId));

        Deposit depositInstruction = new Deposit(accountBeforeDeposit, depositRequest.amount());
        Account accountAfterDeposit = depositInstruction.execute();
        accountRepository.save(accountAfterDeposit);

        DepositEvent depositEvent = new DepositEvent(
                UUID.randomUUID(),
                accountId,
                depositInstruction.amount(),
                accountBeforeDeposit.balance(),
                accountAfterDeposit.balance(),
                Instant.now(),
                actor
        );

        eventPublisher.publishEvent(depositEvent);

        return new OperationResponse(
                depositEvent.id(),
                depositEvent.accountId(),
                OperationType.DEPOSIT,
                accountAfterDeposit.balance(),
                OperationStatus.COMPLETED
        );
    }
}
