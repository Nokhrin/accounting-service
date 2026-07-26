package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountNotOperableException;
import com.nokhrin.accounting.domain.account.AccountStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record Deposit(
        UUID operationId,
        Account targetAccount,
        BigDecimal amount
) implements Operation<DepositResult> {
    private void validateOperation() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be positive", operationId, amount);
        }
        if (targetAccount.status() != AccountStatus.ACTIVE) {
            throw new AccountNotOperableException(targetAccount, getClass().getSimpleName());
        }
    }

    public DepositResult execute() {
        validateOperation();
        Account targetAccountAfter = new Account(
                targetAccount.id(),
                targetAccount.balance().add(amount),
                targetAccount.status(),
                targetAccount.holder()
        );

        return new DepositResult(
                operationId,
                targetAccountAfter
        );
    }

}
