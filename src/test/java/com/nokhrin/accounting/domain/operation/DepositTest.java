package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DepositTest {
    @Test
    void execute_activeAccount_returnsNewAccountWithUpdatedBalance() {
        Account accountInitial = Account.create(new BigDecimal("100"));
        Deposit deposit = new Deposit(accountInitial, new BigDecimal("50"));
        Account accountDeposited = deposit.execute();

        assertAll(
                () -> assertEquals(new BigDecimal("150"), accountDeposited.balance()),
                () -> assertEquals(accountInitial.id(), accountDeposited.id()),
                () -> assertEquals(accountInitial.createdAt(), accountDeposited.createdAt()),
                () -> assertNotSame(accountInitial, accountDeposited)
        );
    }

    @Test
    void execute_blockedAccount_throwsIllegalStateException(){
        Account blocked = Account.create(BigDecimal.valueOf(123.45)).block();
        Deposit deposit = new Deposit(blocked, new BigDecimal("50"));
        assertThrows(IllegalStateException.class, deposit::execute);
    }

    @Test
    void execute_negativeAmount_throwsIllegalArgumentException(){
        Account account = Account.create(BigDecimal.valueOf(123.45));
        Deposit negativeDeposit = new Deposit(account, new BigDecimal("-50"));
        assertThrows(IllegalArgumentException.class, negativeDeposit::execute);
    }

    @Test
    void execute_zeroAmount_throwsIllegalArgumentException(){
        Account account = Account.create(BigDecimal.valueOf(123.45));
        Deposit zeroDeposit = new Deposit(account, BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, zeroDeposit::execute);
    }

}