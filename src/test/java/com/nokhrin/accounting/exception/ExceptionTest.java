package com.nokhrin.accounting.exception;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void accountNotFoundException_messageContainsAccountId() {
        UUID accountId = UUID.randomUUID();
        AccountNotFoundException exception = new AccountNotFoundException(accountId);

        assertTrue(exception.getMessage().contains(accountId.toString()));
    }

    @Test
    void insufficientFundsException_messageContainsDetails() {
        UUID accountId = UUID.randomUUID();
        BigDecimal requested = new BigDecimal("1000");
        BigDecimal available = new BigDecimal("500");

        InsufficientFundsException exception = new InsufficientFundsException(accountId, requested, available);

        assertTrue(exception.getMessage().contains(accountId.toString()));
        assertTrue(exception.getMessage().contains("1000"));
        assertTrue(exception.getMessage().contains("500"));
    }
}