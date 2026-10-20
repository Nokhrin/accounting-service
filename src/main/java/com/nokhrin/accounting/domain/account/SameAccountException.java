package com.nokhrin.accounting.domain.account;

import java.util.UUID;

public class SameAccountException extends RuntimeException {
    public SameAccountException(UUID accountId) {

        super("Source and target account must be different. Account: " + accountId);
    }
}
