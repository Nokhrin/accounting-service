package com.nokhrin.accounting.domain.operation;
import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.infrastructure.dto.DepositRequest;
import com.nokhrin.accounting.infrastructure.persistence.AccountRepositoryAdapter;
import com.nokhrin.accounting.infrastructure.repository.AccountRepository;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationPropertyTest {

    @Property
    void deposit_balanceNonNegative(
        @ForAll @Positive BigDecimal initialBalance,
        @ForAll @Positive BigDecimal depositAmount
    ){
        Account initialAccount = Account.create(initialBalance);
        Deposit deposit = new Deposit(initialAccount, depositAmount);
        Account depositedAccount = deposit.execute();

        assertTrue(depositedAccount.balance().compareTo(BigDecimal.ZERO)>=0);
    }

    @Property
    void deposit_balanceEqualsInitialAddAmount(
        @ForAll @Positive BigDecimal initialBalance,
        @ForAll @Positive BigDecimal depositAmount
    ){
        Account initialAccount = Account.create(initialBalance);
        Deposit deposit = new Deposit(initialAccount, depositAmount);
        Account depositedAccount = deposit.execute();

        assertEquals(initialBalance.add(deposit.amount()), depositedAccount.balance());
    }
}