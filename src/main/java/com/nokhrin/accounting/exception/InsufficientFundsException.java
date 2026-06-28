package com.nokhrin.accounting.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException{
    public InsufficientFundsException(
            UUID accountId, BigDecimal requested, BigDecimal available){

        super(String.format(
                "Insufficient funds in account %s\n" +
                        "Requested: %s, Available: %s",
                accountId, requested, available));
    }
}