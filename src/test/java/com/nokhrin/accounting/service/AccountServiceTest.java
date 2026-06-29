package com.nokhrin.accounting.service;

import com.nokhrin.accounting.dto.AccountResponse;
import com.nokhrin.accounting.dto.CreateAccountRequest;
import com.nokhrin.accounting.entity.Account;
import com.nokhrin.accounting.entity.AccountStatus;
import com.nokhrin.accounting.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountAccount_validRequest_returnsCreatedResponse() {
        CreateAccountRequest request = new CreateAccountRequest(new BigDecimal("1000.00"));
        Account saved = new Account(new BigDecimal("1000.00"));
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("1000.00"), response.balance());
        assertEquals(AccountStatus.ACTIVE, response.status());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createAccountAccount_zeroBalance_returnsActiveAccount() {
        CreateAccountRequest request = new CreateAccountRequest(BigDecimal.ZERO);
        Account saved = new Account(BigDecimal.ZERO);
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        AccountResponse response = accountService.createAccount(request);

        assertEquals(BigDecimal.ZERO, response.balance());
        assertEquals(AccountStatus.ACTIVE, response.status());
    }

    @Test
    void createAccountAccount_negativeBalance_throwsException() {
        CreateAccountRequest request = new CreateAccountRequest(new BigDecimal("-100"));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.createAccount(request));

        verify(accountRepository, never()).save(any());
    }
}