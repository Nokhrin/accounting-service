package com.nokhrin.accounting.infrastructure.service;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountNotFoundException;
import com.nokhrin.accounting.domain.event.AccountCreatedEvent;
import com.nokhrin.accounting.domain.operation.Operation;
import com.nokhrin.accounting.domain.operation.OperationResult;
import com.nokhrin.accounting.domain.operation.SingleOperationResult;
import com.nokhrin.accounting.domain.operation.TransferOperationResult;
import com.nokhrin.accounting.domain.shared.Actor;
import com.nokhrin.accounting.infrastructure.dto.AccountResponse;
import com.nokhrin.accounting.infrastructure.dto.CreateAccountRequest;
import com.nokhrin.accounting.infrastructure.repository.AccountRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    //region Account operations
    public AccountResponse createAccount(CreateAccountRequest request, Actor actor){
        Account account = Account.create(request.initialBalance());
        accountRepository.save(account);
        Instant ts = Instant.now();
        eventPublisher.publishEvent(new AccountCreatedEvent(
                UUID.randomUUID(),
                account.id(),
                account.balance(),
                ts,
                actor
        ));
        return new AccountResponse(
                account.id(),
                account.balance(),
                account.status(),
                ts
        );
    }
    //endregion

    //region Balance operations
    public BigDecimal getBalance(UUID accountId){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(()-> new AccountNotFoundException(accountId));
        return account.balance();
    }

    public OperationResult execute(Operation operation) {
        OperationResult operationResult = operation.execute();

        switch (operationResult) {
            case SingleOperationResult result ->{
                    accountRepository.save(result.accountAfter());
            }
            case TransferOperationResult result -> {
                accountRepository.save(result.sourceAfter());
                accountRepository.save(result.targetAfter());
            }
        }

        return operationResult;
    }
    //endregion
}
