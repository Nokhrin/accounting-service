package com.nokhrin.accounting.infrastructure.service;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountNotFoundException;
import com.nokhrin.accounting.domain.event.AccountCreatedEvent;
import com.nokhrin.accounting.domain.event.DepositEvent;
import com.nokhrin.accounting.domain.event.TransferEvent;
import com.nokhrin.accounting.domain.event.WithdrawEvent;
import com.nokhrin.accounting.domain.operation.*;
import com.nokhrin.accounting.infrastructure.dto.*;
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

    private void saveToRepository(OperationResult result) {
        switch (result) {
            case SingleOperationResult res -> accountRepository.save(res.accountAfter());
            case TransferOperationResult res -> {
                accountRepository.save(res.sourceAfter());
                accountRepository.save(res.targetAfter());
            }
        }
    }

    //region Account
    public AccountResponse createAccount(CreateAccountRequest request, Initiator initiator) {
        Account account = Account.create(request.initialBalance(), new AccountHolder(initiator.id()));
        accountRepository.save(account);
        eventPublisher.publishEvent(new AccountCreatedEvent(
                UUID.randomUUID(),
                account.id(),
                account.balance(),
                Instant.now(),
                initiator
        ));
        return AccountResponse.from(account);
    }
    //endregion

    //region Balance
    public BigDecimal getBalance(UUID accountId){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(()->new AccountNotFoundException(accountId));
        return account.balance();
    }

    public OperationResponse deposit(
            UUID accountId,
            DepositRequest request,
            Initiator initiator
    ){
        Account accountBeforeDeposit = accountRepository.findById(accountId)
                .orElseThrow(()->new AccountNotFoundException(accountId));
        Deposit deposit = new Deposit(accountBeforeDeposit, request.amount());
        SingleOperationResult result = deposit.execute();
        saveToRepository(result);
        Account accountAfterDeposit = result.accountAfter();

        eventPublisher.publishEvent(new DepositEvent(
                result.operationId(),
                result.accountAfter().id(),
                request.amount(),
                accountBeforeDeposit.balance(),
                accountAfterDeposit.balance(),
                Instant.now(),
                initiator
        ));

        return OperationResponse.single(
                result.operationId(),
                OperationType.DEPOSIT,
                accountAfterDeposit
        );
    }

    public OperationResponse withdraw(
            UUID accountId,
            WithdrawRequest request,
            Initiator initiator
    ){
        Account accountBeforeWithdraw = accountRepository.findById(accountId)
                .orElseThrow(()->new AccountNotFoundException(accountId));
        Withdraw withdraw = new Withdraw(accountBeforeWithdraw, request.amount());
        SingleOperationResult result = withdraw.execute();
        saveToRepository(result);
        Account accountAfterWithdraw = result.accountAfter();

        eventPublisher.publishEvent(new WithdrawEvent(
                result.operationId(),
                result.accountAfter().id(),
                request.amount(),
                accountBeforeWithdraw.balance(),
                accountAfterWithdraw.balance(),
                Instant.now(),
                initiator
        ));

        return OperationResponse.single(
                result.operationId(),
                OperationType.WITHDRAWAL,
                accountAfterWithdraw
        );
    }

    public OperationResponse transfer(
            UUID sourceAccountId,
            UUID targetAccountId,
            TransferRequest request,
            Initiator initiator
    ){
        Account sourceAccountBeforeTransfer =accountRepository.findById(sourceAccountId)
                .orElseThrow(()->new AccountNotFoundException(sourceAccountId));
        Account targetAccountBeforeTransfer =accountRepository.findById(targetAccountId)
                .orElseThrow(()->new AccountNotFoundException(targetAccountId));

        Transfer transfer = new Transfer(
                sourceAccountBeforeTransfer,
                targetAccountBeforeTransfer,
                request.amount());

        TransferOperationResult result = transfer.execute();
        saveToRepository(result);

        eventPublisher.publishEvent(new TransferEvent(
                result.operationId(),
                request.amount(),
                sourceAccountId,
                targetAccountId,
                sourceAccountBeforeTransfer.balance(),
                result.sourceAfter().balance(),
                targetAccountBeforeTransfer.balance(),
                result.targetAfter().balance(),
                Instant.now(),
                initiator
        ));

        return OperationResponse.transfer(
                result.operationId(),
                OperationType.TRANSFER,
                result.sourceAfter(),
                result.targetAfter()
        );
    }
    //endregion
}
