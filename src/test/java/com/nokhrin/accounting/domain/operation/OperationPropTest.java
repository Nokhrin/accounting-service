
package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountStatus;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class OperationPropTest {
    @Provide
    Arbitrary<BigDecimal> nonNegativeBigDecimal(){
        return Arbitraries.bigDecimals()
                .between(BigDecimal.ZERO, new BigDecimal("1000000000000.00"))
                .ofScale(2);
    }

    private final AccountHolder HOLDER = new AccountHolder(UUID.randomUUID(), "Operation Test Holder");

    @Property
    void deposit_balanceIncreasedExactlyByAmount(
            @ForAll @From("nonNegativeBigDecimal") BigDecimal initialBalance,
            @ForAll @Positive BigDecimal amount
    ) {
        Account accountBefore = new Account(UUID.randomUUID(), initialBalance, AccountStatus.ACTIVE, HOLDER);
        Deposit deposit = new Deposit(UUID.randomUUID(), accountBefore, amount);
        DepositResult depositResult = deposit.execute();
        assertEquals(initialBalance.add(deposit.amount()), depositResult.accountAfter().balance());
    }

    @Property
    void withdraw_balanceDecreasedExactlyByAmount(
            @ForAll @From("nonNegativeBigDecimal") BigDecimal initialBalance,
            @ForAll @Positive BigDecimal amount
    ) {
        assumeTrue(initialBalance.compareTo(amount) >= 0);

        Account accountBefore = new Account(UUID.randomUUID(), initialBalance, AccountStatus.ACTIVE, HOLDER);
        Withdrawal withdrawal = new Withdrawal(UUID.randomUUID(), accountBefore, amount);
        WithdrawalResult withdrawalResult = withdrawal.execute();
        assertEquals(initialBalance.subtract(withdrawal.amount()), withdrawalResult.accountAfter().balance());
    }

    @Property
    void transfer_totalBalanceConstant(
            @ForAll @From("nonNegativeBigDecimal") BigDecimal initialSourceBalance,
            @ForAll @From("nonNegativeBigDecimal") BigDecimal initialTargetBalance,
            @ForAll @Positive BigDecimal amount
    ) {
        assumeTrue(initialSourceBalance.compareTo(amount) >= 0);

        Account sourceBefore = new Account(UUID.randomUUID(), initialSourceBalance, AccountStatus.ACTIVE, HOLDER);
        Account targetBefore = new Account(UUID.randomUUID(), initialTargetBalance, AccountStatus.ACTIVE, HOLDER);
        Transfer transfer = new Transfer(UUID.randomUUID(), sourceBefore, targetBefore, amount);
        TransferResult transferResult = transfer.execute();
        assertEquals(
                sourceBefore.balance().add(targetBefore.balance()),
                transferResult.sourceAccountAfter().balance().add(transferResult.targetAccountAfter().balance())
        );
    }
}