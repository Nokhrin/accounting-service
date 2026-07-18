package com.nokhrin.accounting.domain.operation;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientBalanceException extends RuntimeException{
    public InsufficientBalanceException(
            UUID accountId, BigDecimal available, BigDecimal requested){

        super(String.format(
                """
                        Insufficient funds in targetAccount %s
                        Available: %s
                        Requested: %s
                        """,
                accountId, requested, available));
    }
}