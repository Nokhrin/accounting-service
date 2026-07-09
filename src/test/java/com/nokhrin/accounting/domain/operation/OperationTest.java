package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OperationTest {
    private final AccountHolder HOLDER = new AccountHolder(UUID.fromString("00000000-0000-0000-0000-000000000001"), "test_holder_1");

    @Test
    void deposit_valid_executes(){
        Account account = Account.create(BigDecimal.ZERO, HOLDER);
        Deposit deposit= new Deposit(account, BigDecimal.ONE);
        DepositResult result = deposit.execute();
        assertEquals(BigDecimal.ONE, result.accountAfter().balance());
    }

    @Test
    void deposit_inactive_throwsException(){
        Account blocked = Account.create(new BigDecimal(100), HOLDER).block();
        Account closed = Account.create(BigDecimal.ZERO, HOLDER).close();

        assertAll(
                ()->assertThrows(IllegalStateException.class, ()-> new Deposit(blocked, BigDecimal.TEN).execute()),
                ()->assertThrows(IllegalStateException.class, ()-> new Deposit(closed, BigDecimal.TEN).execute())
        );
    }

    @Test
    void withdraw_sufficient_executes(){
        Account account = Account.create(BigDecimal.ONE, HOLDER);
        Withdrawal withdrawal=new Withdrawal(account, BigDecimal.ONE);
        WithdrawalResult result= withdrawal.execute();
        assertEquals(BigDecimal.ZERO, result.accountAfter().balance());
    }

    @Test
    void withdraw_insufficient_throws(){
        Account account = Account.create(BigDecimal.ONE, HOLDER);
        assertThrows(IllegalArgumentException.class,
                ()-> new Withdrawal(account, BigDecimal.TEN).execute());
    }

    @Test
    void transfer_valid_executes(){
        Account sourceAccount = Account.create(BigDecimal.ONE, HOLDER);
        Account targetAccount = Account.create(BigDecimal.ZERO, HOLDER);
        Transfer transfer=new Transfer(sourceAccount, targetAccount, BigDecimal.ONE);
        TransferResult result = transfer.execute();
        assertAll(
                ()->assertEquals(BigDecimal.ZERO, result.sourceAfter().balance()),
                ()->assertEquals(BigDecimal.ONE, result.targetAfter().balance())
        );
    }

    @Test
    void transfer_insufficient_throws(){
        Account sourceAccount = Account.create(BigDecimal.ONE, HOLDER);
        Account targetAccount = Account.create(BigDecimal.ZERO, HOLDER);
        assertThrows(IllegalArgumentException.class,
                ()->new Transfer(sourceAccount, targetAccount, BigDecimal.TEN).execute());
    }

    @Test
    void transfer_sourceBlocked_throws(){
        Account sourceAccountBlocked = Account.create(BigDecimal.ONE, HOLDER).block();
        Account targetAccount = Account.create(BigDecimal.ZERO, HOLDER);
        assertThrows(IllegalStateException.class,
                ()->new Transfer(sourceAccountBlocked, targetAccount, BigDecimal.ONE).execute()
        );
    }

    @Test
    void transfer_targetClosed_throws(){
        Account sourceAccount = Account.create(BigDecimal.ONE, HOLDER);
        Account targetAccountClosed = Account.create(BigDecimal.ZERO, HOLDER).close();
        assertThrows(IllegalStateException.class,
                ()->new Transfer(sourceAccount, targetAccountClosed, BigDecimal.ONE).execute()
        );
    }
}