package com.nokhrin.accounting.domain.operation;

import com.nokhrin.accounting.domain.account.Account;

import java.math.BigDecimal;
import java.util.UUID;

public record Transfer(
        UUID operationId,
        Account sourceAccount,
        Account targetAccount,
        BigDecimal amount
) implements Operation<TransferResult> {
    private void validateOperation() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be positive", operationId, amount);
        }
    }

    public TransferResult execute() {
        validateOperation();

        Deposit deposit = new Deposit(UUID.randomUUID(), targetAccount, amount);
        Withdrawal withdrawal = new Withdrawal(UUID.randomUUID(), sourceAccount, amount);

        DepositResult depositResult = deposit.execute();
        WithdrawalResult withdrawalResult = withdrawal.execute();

        return new TransferResult(
                UUID.randomUUID(),
                depositResult.accountAfter(),
                withdrawalResult.accountAfter()
        );
    }
}
