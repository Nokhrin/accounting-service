package com.nokhrin.accounting.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InvalidTransactionException extends RuntimeException{
    public InvalidTransactionException(String message){
        super(message);
    }
}