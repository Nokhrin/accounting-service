package com.nokhrin.accounting.api;

import com.nokhrin.accounting.application.AccountService;
import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.operation.DepositResult;
import com.nokhrin.accounting.domain.operation.TransferResult;
import com.nokhrin.accounting.domain.operation.WithdrawalResult;
import com.nokhrin.accounting.generated.api.AccountsApi;
import com.nokhrin.accounting.generated.model.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
public class AccountController implements AccountsApi {
    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public ResponseEntity<AccountResponse> openAccount(@Valid @RequestBody OpenAccountRequest openAccountRequest) {
        
        AccountHolder holder = new AccountHolder(
                UUID.randomUUID(),
                openAccountRequest.getHolder().getDisplayName()
        );
        Account account = accountService.openAccount(
                openAccountRequest.getInitialBalance(),
                holder,
                openAccountRequest.getOperationId()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/accounts/" + account.id()))
                .body(AccountResponseMapper.from(account));
    }

    @Override
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID accountId) {
        Account account = accountService.getAccount(accountId);
        return ResponseEntity.ok(AccountResponseMapper.from(account));
    }

    @Override
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable UUID accountId) {
        Account closedAccount = accountService.closeAccount(accountId);
        return ResponseEntity.ok(AccountResponseMapper.from(closedAccount));
    }

    @Override
    public ResponseEntity<DepositResponse> deposit(
            @PathVariable UUID targetAccountId,
            @Valid @RequestBody AmountRequest amountRequest
    ) {
        DepositResult deposit = accountService.deposit(targetAccountId, amountRequest.getAmount());
        DepositResponse response = new DepositResponse(
                deposit.operationId(),
                AccountResponseMapper.from(deposit.accountAfter())
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<WithdrawalResponse> withdraw(
            @PathVariable UUID sourceAccountId,
            @Valid @RequestBody AmountRequest amountRequest
    ) {
        WithdrawalResult withdraw = accountService.withdraw(sourceAccountId, amountRequest.getAmount());
        WithdrawalResponse response = new WithdrawalResponse(
                withdraw.operationId(),
                AccountResponseMapper.from(withdraw.accountAfter())
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<TransferResponse> transfer(
            @PathVariable UUID sourceAccountId,
            @PathVariable UUID targetAccountId,
            @Valid @RequestBody AmountRequest amountRequest
    ) {
        TransferResult transfer = accountService.transfer(sourceAccountId, targetAccountId, amountRequest.getAmount());
        TransferResponse response = new TransferResponse(
                transfer.operationId(),
                AccountResponseMapper.from(transfer.sourceAccountAfter()),
                AccountResponseMapper.from(transfer.targetAccountAfter())
        );
        return ResponseEntity.ok(response);
    }

}
