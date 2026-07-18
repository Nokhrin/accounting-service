package com.nokhrin.accounting.domain.account;

public class AccountNotOperableException extends RuntimeException{
    public AccountNotOperableException(Account account, String operationName){
        super("Cannot execute operation " + operationName + "\nAccount Id: " + account.id() + "\nStatus: " + account.status());
    }
}