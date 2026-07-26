package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountNotOperableException;
import com.nokhrin.accounting.domain.account.AccountStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record Withdrawal(
        UUID operationId,
        Account sourceAccount,
        BigDecimal amount
) implements Operation<WithdrawalResult> {
    private void validateOperation() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be positive", operationId, amount);
        }
        if (sourceAccount.status() != AccountStatus.ACTIVE) {
            throw new AccountNotOperableException(sourceAccount, getClass().getSimpleName());
        }
        if (sourceAccount.balance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(sourceAccount.id(), sourceAccount.balance(), amount);
        }
    }

    public WithdrawalResult execute() {
        validateOperation();
        Account sourceAccountAfter = new Account(
                sourceAccount.id(),
                sourceAccount.balance().subtract(amount),
                sourceAccount.status(),
                sourceAccount.holder()
        );

        return new WithdrawalResult(
                operationId,
                sourceAccountAfter
        );
    }
}
