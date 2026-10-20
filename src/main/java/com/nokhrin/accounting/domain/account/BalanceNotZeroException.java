package com.nokhrin.accounting.domain.account;

import java.math.BigDecimal;
import java.util.UUID;

public class BalanceNotZeroException extends RuntimeException {
    public BalanceNotZeroException(UUID accountId, BigDecimal balance) {

        super("Cannot close an account with non-zero balance. Account: " + accountId + ", balance: " + balance);
    }
}
