package com.nokhrin.accounting.domain.operation;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException{
    public InsufficientFundsException(
            UUID accountId, BigDecimal requested, BigDecimal available){

        super(String.format(
                "Insufficient funds in targetAccount %s\n" +
                        "Requested: %s, Available: %s",
                accountId, requested, available));
    }
}