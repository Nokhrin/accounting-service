package com.nokhrin.accounting.application;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountNotFoundException;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.event.EventRepository;
import com.nokhrin.accounting.domain.event.FundsDeposited;
import com.nokhrin.accounting.domain.event.FundsWithdrawn;
import com.nokhrin.accounting.domain.event.TransferCompleted;
import com.nokhrin.accounting.domain.operation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final EventRepository eventRepository;

    public AccountService(AccountRepository accountRepository, EventRepository eventRepository) {
        this.accountRepository = accountRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public DepositResult deposit(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(()-> new AccountNotFoundException(accountId));

        UUID operationId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Deposit depositOperation = new Deposit(operationId, account, amount);
        DepositResult result = depositOperation.execute();
        Instant executedAt = Instant.now();

        accountRepository.update(result.accountAfter());

        Instant recordedAt = Instant.now();
        FundsDeposited event = new FundsDeposited(eventId, operationId, amount, accountId, executedAt, recordedAt);
        eventRepository.record(event);

        return result;
    }

    @Transactional
    public WithdrawalResult withdraw(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(()-> new AccountNotFoundException(accountId));

        UUID operationId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Withdrawal withdrawal = new Withdrawal(operationId, account, amount);
        WithdrawalResult result = withdrawal.execute();
        Instant executedAt = Instant.now();

        accountRepository.update(result.accountAfter());

        Instant recordedAt = Instant.now();
        FundsWithdrawn event = new FundsWithdrawn(eventId, operationId, amount, accountId, executedAt, recordedAt);
        eventRepository.record(event);

        return result;
    }

    @Transactional
    public TransferResult transfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount) {
        Account sourceAccount = accountRepository.findById(sourceAccountId)
                .orElseThrow(()-> new AccountNotFoundException(sourceAccountId));

        Account targetAccount = accountRepository.findById(targetAccountId)
                .orElseThrow(()-> new AccountNotFoundException(targetAccountId));

        UUID operationId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Transfer transfer = new Transfer(operationId, sourceAccount, targetAccount, amount);
        TransferResult result = transfer.execute();
        Instant executedAt = Instant.now();

        accountRepository.update(result.sourceAccountAfter());
        accountRepository.update(result.targetAccountAfter());

        Instant recordedAt = Instant.now();
        TransferCompleted event = new TransferCompleted(eventId, operationId, amount, sourceAccountId, targetAccountId, executedAt, recordedAt);
        eventRepository.record(event);

        return result;
    }
}
