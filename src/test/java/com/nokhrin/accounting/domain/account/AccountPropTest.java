package com.nokhrin.accounting.domain.account;

import com.nokhrin.accounting.domain.operation.Deposit;
import com.nokhrin.accounting.domain.operation.DepositResult;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountPropTest {

    private final AccountHolder HOLDER = new AccountHolder(UUID.fromString("00000000-0000-0000-0000-000000000001"), "test_holder_1");

    @Property
    void account_balanceNeverNegative_afterDeposit(
            @ForAll @Positive BigDecimal initialBalance,
            @ForAll @Positive BigDecimal depositAmount
    ) {
        Account account = Account.create(initialBalance, HOLDER);
        Deposit deposit = new Deposit(account, depositAmount);
        DepositResult result = deposit.execute();
        assertTrue(result.accountAfter().balance().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Property
    void deposit_newAccount_noMutation(
            @ForAll @Positive BigDecimal initialBalance,
            @ForAll @Positive BigDecimal depositAmount
    ){
        Account accountInitial = Account.create(initialBalance, HOLDER);
        DepositResult result = new Deposit(accountInitial, depositAmount).execute();
        assertAll(
                ()->assertNotSame(accountInitial, result.accountAfter()),
                ()->assertEquals(initialBalance.add(depositAmount), result.accountAfter().balance()),
                ()->assertEquals(accountInitial.id(), result.accountAfter().id()),
                ()->assertEquals(accountInitial.createdAt(), result.accountAfter().createdAt())
        );
    }
}