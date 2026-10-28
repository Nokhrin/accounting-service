package com.nokhrin.accounting.application;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountNotFoundException;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.event.*;
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
    private final OperationRegistry operationRegistry;

    public AccountService(AccountRepository accountRepository, EventRepository eventRepository, OperationRegistry operationRegistry) {
        this.accountRepository = accountRepository;
        this.eventRepository = eventRepository;
        this.operationRegistry = operationRegistry;
    }


    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }


    @Transactional
    public DepositResult deposit(UUID accountId, BigDecimal amount, UUID operationId) {
        operationRegistry.register(operationId, accountId, OperationType.DEPOSIT);

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

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
    public WithdrawalResult withdraw(UUID accountId, BigDecimal amount, UUID operationId) {
        operationRegistry.register(operationId, accountId, OperationType.WITHDRAWAL);

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

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
    public TransferResult transfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, UUID operationId) {
        operationRegistry.register(operationId, sourceAccountId, OperationType.TRANSFER);

        Account sourceAccount = accountRepository.findByIdForUpdate(sourceAccountId)
                .orElseThrow(() -> new AccountNotFoundException(sourceAccountId));

        Account targetAccount = accountRepository.findByIdForUpdate(targetAccountId)
                .orElseThrow(() -> new AccountNotFoundException(targetAccountId));

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

    @Transactional
    public Account openAccount(BigDecimal initialBalance, AccountHolder holder, UUID operationId) {
        Account account = Account.open(initialBalance, holder);
        operationRegistry.register(operationId, account.id(), OperationType.OPEN_ACCOUNT);

        UUID eventId = UUID.randomUUID();
        Instant executedAt = Instant.now();

        accountRepository.create(account);
        eventRepository.record(new AccountOpened(eventId, account.id(), executedAt, Instant.now()));

        return account;
    }

    @Transactional
    public Account closeAccount(UUID accountId) {
        UUID operationId = UUID.randomUUID();
        operationRegistry.register(operationId, accountId, OperationType.CLOSE_ACCOUNT);
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        UUID eventId = UUID.randomUUID();
        Account closedAccount = account.close();
        Instant closedAt = Instant.now();
        accountRepository.update(closedAccount);
        Instant recordedAt = Instant.now();

        eventRepository.record(new AccountClosed(
                eventId,
                closedAccount.id(),
                closedAt,
                recordedAt
        ));
        return closedAccount;
    }
}
