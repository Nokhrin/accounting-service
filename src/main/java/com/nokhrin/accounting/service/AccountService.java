package com.nokhrin.accounting.service;

import com.nokhrin.accounting.dto.AccountResponse;
import com.nokhrin.accounting.dto.CreateAccountRequest;
import com.nokhrin.accounting.entity.Account;
import com.nokhrin.accounting.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository){
        this.accountRepository=accountRepository;
    }

    public AccountResponse createAccount(CreateAccountRequest request){
        Account account=new Account(request.initialBalance());
        Account saved = accountRepository.save(account);
        return AccountResponse.fromEntity(saved);
    }
}
