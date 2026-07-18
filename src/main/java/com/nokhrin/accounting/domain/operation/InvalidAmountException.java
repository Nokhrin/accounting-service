package com.nokhrin.accounting.domain.operation;

import java.math.BigDecimal;
import java.util.UUID;

public class InvalidAmountException extends RuntimeException{
    public InvalidAmountException(
            String description, UUID operationId, BigDecimal amount){
        super(String.format(description + "\namount provided: " + amount + "\noperation: " + operationId));
    }
}