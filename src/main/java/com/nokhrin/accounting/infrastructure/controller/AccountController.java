package com.nokhrin.accounting.infrastructure.controller;

import com.nokhrin.accounting.domain.operation.Deposit;
import com.nokhrin.accounting.domain.shared.Actor;
import com.nokhrin.accounting.domain.shared.CustomerActor;
import com.nokhrin.accounting.infrastructure.dto.*;
import com.nokhrin.accounting.infrastructure.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        return null;
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @PathVariable UUID id) {
        return null;
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<OperationResponse> deposit(
            @PathVariable UUID id,
            @Valid @RequestBody DepositRequest request,
            Principal principal
            ) {
        return null;
    }
}