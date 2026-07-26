package com.nokhrin.accounting.api;

import com.nokhrin.accounting.api.dto.AmountRequest;
import com.nokhrin.accounting.api.dto.OpenAccountRequest;
import com.nokhrin.accounting.api.dto.Response;
import com.nokhrin.accounting.application.AccountService;
import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.operation.DepositResult;
import com.nokhrin.accounting.domain.operation.TransferResult;
import com.nokhrin.accounting.domain.operation.WithdrawalResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Response> openAccount(@Valid @RequestBody OpenAccountRequest request) {
        Account account = accountService.openAccount(request.initialBalance(), request.holder());
        Response response=account.toResponse();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getAccount(@PathVariable UUID accountId){
        Account account=accountService.getAccount(accountId);
        return ResponseEntity.ok(account.toResponse());
    }

    @PostMapping("/{targetId}/deposit")
    public ResponseEntity<Response> deposit(
            @PathVariable UUID targetId,
            @Valid @RequestBody AmountRequest request
            ){
        DepositResult result=accountService.deposit(targetId, request.amount());
        return ResponseEntity.ok(result.accountAfter().toResponse());
    }

    @PostMapping("/{sourceId}/withdraw")
    public ResponseEntity<Response> withdraw(
            @PathVariable UUID sourceId,
            @Valid @RequestBody AmountRequest request
            ){
        WithdrawalResult result=accountService.withdraw(sourceId, request.amount());
        return ResponseEntity.ok(result.accountAfter().toResponse());
    }

    @PostMapping("/{sourceId}/{targetId}/transfer")
    public ResponseEntity<Response> transfer(
            @PathVariable UUID sourceId,
            @PathVariable UUID targetId,
            @Valid @RequestBody AmountRequest request
            ){
        TransferResult result=accountService.transfer(sourceId, targetId, request.amount());
        return null;
    }

}
