package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountNotOperableException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OperationTest {
    private final AccountHolder HOLDER = new AccountHolder(UUID.fromString("00000000-0000-0000-0000-000000000001"), "test_holder_1");

    @Test
    void deposit_valid_executes() {
        Account account = Account.open(BigDecimal.ZERO, HOLDER);
        Deposit deposit = new Deposit(UUID.randomUUID(), account, BigDecimal.ONE);
        DepositResult result = deposit.execute();
        assertEquals(BigDecimal.ONE, result.accountAfter().balance());
    }

    @Test
    void deposit_inactive_throwsException() {
        Account blocked = Account.open(new BigDecimal(100), HOLDER).block();
        Account closed = Account.open(BigDecimal.ZERO, HOLDER).close();

        assertAll(
                () -> assertThrows(AccountNotOperableException.class, () -> new Deposit(UUID.randomUUID(), blocked, BigDecimal.TEN).execute()),
                () -> assertThrows(AccountNotOperableException.class, () -> new Deposit(UUID.randomUUID(), closed, BigDecimal.TEN).execute())
        );
    }

    @Test
    void withdraw_sufficient_executes() {
        Account account = Account.open(BigDecimal.ONE, HOLDER);
        Withdrawal withdrawal = new Withdrawal(UUID.randomUUID(), account, BigDecimal.ONE);
        WithdrawalResult result = withdrawal.execute();
        assertEquals(BigDecimal.ZERO, result.accountAfter().balance());
    }

    @Test
    void withdraw_insufficient_throws() {
        Account account = Account.open(BigDecimal.ONE, HOLDER);
        assertThrows(InsufficientBalanceException.class,
                () -> new Withdrawal(UUID.randomUUID(), account, BigDecimal.TEN).execute());
    }

    @Test
    void transfer_valid_executes() {
        Account sourceAccount = Account.open(BigDecimal.ONE, HOLDER);
        Account targetAccount = Account.open(BigDecimal.ZERO, HOLDER);
        Transfer transfer = new Transfer(UUID.randomUUID(), sourceAccount, targetAccount, BigDecimal.ONE);
        TransferResult result = transfer.execute();
        assertAll(
                () -> assertEquals(BigDecimal.ZERO, result.sourceAccountAfter().balance()),
                () -> assertEquals(BigDecimal.ONE, result.targetAccountAfter().balance())
        );
    }

    @Test
    void transfer_insufficient_throws() {
        Account sourceAccount = Account.open(BigDecimal.ONE, HOLDER);
        Account targetAccount = Account.open(BigDecimal.ZERO, HOLDER);
        assertThrows(InsufficientBalanceException.class,
                () -> new Transfer(UUID.randomUUID(), sourceAccount, targetAccount, BigDecimal.TEN).execute());
    }

    @Test
    void transfer_sourceBlocked_throws() {
        Account sourceAccountBlocked = Account.open(BigDecimal.ONE, HOLDER).block();
        Account targetAccount = Account.open(BigDecimal.ZERO, HOLDER);
        assertThrows(AccountNotOperableException.class,
                () -> new Transfer(UUID.randomUUID(), sourceAccountBlocked, targetAccount, BigDecimal.ONE).execute()
        );
    }

    @Test
    void transfer_targetClosed_throws() {
        Account sourceAccount = Account.open(BigDecimal.ONE, HOLDER);
        Account targetAccountClosed = Account.open(BigDecimal.ZERO, HOLDER).close();
        assertThrows(AccountNotOperableException.class,
                () -> new Transfer(UUID.randomUUID(), sourceAccount, targetAccountClosed, BigDecimal.ONE).execute()
        );
    }
}