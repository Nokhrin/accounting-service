package com.nokhrin.accounting.infrastructure.controller;

import com.nokhrin.accounting.domain.operation.CustomerInitiator;
import com.nokhrin.accounting.domain.operation.Initiator;
import com.nokhrin.accounting.infrastructure.dto.*;
import com.nokhrin.accounting.infrastructure.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
            @Valid @RequestBody CreateAccountRequest request,
            Principal principal
    ) {
        Initiator initiator = new CustomerInitiator(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request, initiator));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getBalance(id));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<OperationResponse> deposit(
            @PathVariable UUID id,
            @Valid @RequestBody DepositRequest request,
            Principal principal
            ) {
        Initiator initiator = new CustomerInitiator(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.deposit(id, request, initiator));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<OperationResponse> withdraw(
            @PathVariable UUID id,
            @Valid @RequestBody WithdrawRequest request,
            Principal principal
    ) {
        Initiator initiator = new CustomerInitiator(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.withdraw(id, request, initiator));
    }

    @PostMapping("/{sourceId}/{targetId}/transfer")
    public ResponseEntity<OperationResponse> transfer(
            @PathVariable UUID sourceId,
            @PathVariable UUID targetId,
            @Valid @RequestBody TransferRequest request,
            Principal principal
    ) {
        Initiator initiator = new CustomerInitiator(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.transfer(sourceId, targetId, request, initiator));
    }
}