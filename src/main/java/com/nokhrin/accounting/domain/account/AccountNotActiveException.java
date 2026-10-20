package com.nokhrin.accounting.domain.account;

import java.util.UUID;

public class AccountNotActiveException extends RuntimeException {
    public AccountNotActiveException(UUID accountId, AccountStatus accountStatus) {

        super("Account " + accountId + " is not active, current status: " + accountStatus);
    }
}
