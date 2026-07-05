package com.nokhrin.accounting.domain.account;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException{
    public AccountNotFoundException(UUID accountId){
        super("Account not found: " + accountId);
    }
}