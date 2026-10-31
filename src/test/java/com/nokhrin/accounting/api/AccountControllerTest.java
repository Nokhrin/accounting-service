package com.nokhrin.accounting.api;

import com.nokhrin.accounting.application.AccountService;
import com.nokhrin.accounting.domain.account.*;
import com.nokhrin.accounting.domain.operation.DepositResult;
import com.nokhrin.accounting.domain.operation.DuplicateOperationException;
import com.nokhrin.accounting.domain.operation.TransferResult;
import com.nokhrin.accounting.domain.operation.WithdrawalResult;
import com.nokhrin.accounting.generated.model.AccountHolderRequest;
import com.nokhrin.accounting.generated.model.AmountRequest;
import com.nokhrin.accounting.generated.model.OpenAccountRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureJson
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    private final AccountHolder holder = new AccountHolder(UUID.randomUUID(), "Test User");
    private final UUID accountId = UUID.randomUUID();
    private final UUID operationId = UUID.randomUUID();

    private OpenAccountRequest openAccountRequest(BigDecimal amount, AccountHolderRequest holder) {
        return new OpenAccountRequest()
                .operationId(operationId)
                .initialBalance(amount)
                .holder(holder);
    }

    private AmountRequest amountRequest(BigDecimal amount) {
        return new AmountRequest()
                .amount(amount)
                .operationId(operationId);
    }


    @Test
    void openAccount_validRequest_returns201() throws Exception {
        Account domainAccount = new Account(accountId, BigDecimal.TEN, AccountStatus.ACTIVE, holder);
        when(accountService.openAccount(any(), any(), any()))
                .thenReturn(domainAccount);

        OpenAccountRequest request = openAccountRequest(BigDecimal.TEN, new AccountHolderRequest(holder.displayName()));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.balance").value(10))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.holder.id").value(holder.id().toString()))
                .andExpect(jsonPath("$.holder.displayName").value("Test User"));
    }

    @Test
    void getAccount_exists_returns200() throws Exception {
        Account domainAccount = new Account(accountId, new BigDecimal("100.50"), AccountStatus.ACTIVE, holder);
        when(accountService.getAccount(accountId)).thenReturn(domainAccount);

        mockMvc.perform(get("/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.balance").value(100.50))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getAccount_notFound_returns404() throws Exception {
        when(accountService.getAccount(accountId)).thenThrow(new AccountNotFoundException(accountId));

        mockMvc.perform(get("/accounts/{accountId}", accountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void deposit_zeroAmount_returns400() throws Exception {
        AmountRequest request = amountRequest(BigDecimal.ZERO);
        mockMvc.perform(post("/accounts/{accountId}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void deposit_minimumAmount_returns200() throws Exception {
        Account accountAfter = new Account(accountId, new BigDecimal("0.01"), AccountStatus.ACTIVE, holder);
        when(accountService.deposit(eq(accountId), any(), any()))
                .thenReturn(new DepositResult(UUID.randomUUID(), accountAfter));

        AmountRequest request = amountRequest(new BigDecimal("0.01"));
        mockMvc.perform(post("/accounts/{accountId}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").exists())
                .andExpect(jsonPath("$.account.id").value(accountId.toString()))
                .andExpect(jsonPath("$.account.balance").value(0.01));
    }

    @Test
    void deposit_validRequest_returns200() throws Exception {
        Account accountAfter = new Account(accountId, new BigDecimal("150"), AccountStatus.ACTIVE, holder);
        DepositResult result = new DepositResult(UUID.randomUUID(), accountAfter);

        when(accountService.deposit(eq(accountId), any(), any())).thenReturn(result);

        AmountRequest request = amountRequest(new BigDecimal("50"));

        mockMvc.perform(post("/accounts/{targetId}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").exists())
                .andExpect(jsonPath("$.account.id").value(accountId.toString()))
                .andExpect(jsonPath("$.account.balance").value(150));
    }

    @Test
    void deposit_duplicateOperationId_returns409() throws Exception {
        when(accountService.deposit(eq(accountId), any(), eq(operationId)))
                .thenThrow(new DuplicateOperationException(operationId));

        AmountRequest request = amountRequest(new BigDecimal("50"));

        mockMvc.perform(post("/accounts/{targetId}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_OPERATION"));
    }

    @Test
    void withdraw_validRequest_returns200() throws Exception {
        Account accountAfter = new Account(accountId, new BigDecimal("50"), AccountStatus.ACTIVE, holder);
        WithdrawalResult result = new WithdrawalResult(UUID.randomUUID(), accountAfter);

        when(accountService.withdraw(eq(accountId), any(), any())).thenReturn(result);

        AmountRequest request = amountRequest(new BigDecimal("50"));

        mockMvc.perform(post("/accounts/{sourceId}/withdraw", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").exists())
                .andExpect(jsonPath("$.account.id").value(accountId.toString()))
                .andExpect(jsonPath("$.account.balance").value(50));
    }

    @Test
    void withdraw_duplicateOperationId_returns409() throws Exception {
        when(accountService.withdraw(eq(accountId), any(), eq(operationId)))
                .thenThrow(new DuplicateOperationException(operationId));

        AmountRequest request = amountRequest(new BigDecimal("50"));

        mockMvc.perform(post("/accounts/{sourceId}/withdraw", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_OPERATION"));
    }

    @Test
    void transfer_validRequest_returnsTransferResponse() throws Exception {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Account sourceAfter = new Account(sourceId, BigDecimal.ZERO, AccountStatus.ACTIVE, holder);
        Account targetAfter = new Account(targetId, new BigDecimal("100"), AccountStatus.ACTIVE, holder);
        TransferResult result = new TransferResult(UUID.randomUUID(), sourceAfter, targetAfter);

        when(accountService.transfer(eq(sourceId), eq(targetId), any(), any()))
                .thenReturn(result);

        AmountRequest request = amountRequest(new BigDecimal("100"));

        mockMvc.perform(post("/accounts/{sourceId}/{targetId}/transfer", sourceId, targetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").exists())
                .andExpect(jsonPath("$.sourceAccount.id").value(sourceId.toString()))
                .andExpect(jsonPath("$.targetAccount.id").value(targetId.toString()));
    }

    @Test
    void transfer_accountNotFound_returns404() throws Exception {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(accountService.transfer(eq(sourceId), eq(targetId), any(), any()))
                .thenThrow(new AccountNotFoundException(sourceId));

        AmountRequest request = amountRequest(new BigDecimal("100"));

        mockMvc.perform(post("/accounts/{sourceId}/{targetId}/transfer", sourceId, targetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void transfer_duplicateOperationId_returns409() throws Exception {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(accountService.transfer(eq(sourceId), eq(targetId), any(), eq(operationId)))
                .thenThrow(new DuplicateOperationException(operationId));

        AmountRequest request = amountRequest(new BigDecimal("100"));

        mockMvc.perform(post("/accounts/{sourceId}/{targetId}/transfer", sourceId, targetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_OPERATION"));
    }

    @Test
    void close_zeroBalance_returns200() throws Exception {
        when(accountService.closeAccount(accountId))
                .thenReturn(new Account(accountId, BigDecimal.ZERO, AccountStatus.CLOSED, holder));
        mockMvc.perform(post("/accounts/{accountId}/close", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void closeAccount_activeAccountWithZeroBalance_returnsClosedAccount() throws Exception {
        UUID accountId = UUID.randomUUID();

        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "Test Holder");

        Account closedAccount = new Account(
                accountId,
                BigDecimal.ZERO,
                AccountStatus.CLOSED,
                holder
        );

        when(accountService.closeAccount(accountId)).thenReturn(closedAccount);

        mockMvc.perform(post("/accounts/{accountId}/close", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void closeAccount_nonZeroBalance_returns409BalanceNotZero() throws Exception {
        UUID accountId = UUID.randomUUID();

        when(accountService.closeAccount(accountId))
                .thenThrow(new BalanceNotZeroException(accountId, new BigDecimal("10.00")));

        mockMvc.perform(post("/accounts/{accountId}/close", accountId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BALANCE_NOT_ZERO"));
    }

    @Test
    void closeAccount_notActiveAccount_returns409AccountNotActive() throws Exception {
        UUID accountId = UUID.randomUUID();

        when(accountService.closeAccount(accountId))
                .thenThrow(new AccountNotActiveException(accountId, AccountStatus.BLOCKED));

        mockMvc.perform(post("/accounts/{accountId}/close", accountId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_ACTIVE"));
    }


    @Test
    void openAccount_emptyDisplayName_returns400() throws Exception {
        OpenAccountRequest request = openAccountRequest(
                BigDecimal.TEN,
                new AccountHolderRequest("")
        );
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void openAccount_negativeBalance_returns400() throws Exception {
        OpenAccountRequest request = openAccountRequest(
                new BigDecimal("-1.00"),
                new AccountHolderRequest("Test")
        );
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAccount_malformedUuid_returns400() throws Exception {
        mockMvc.perform(get("/accounts/{accountId}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }


    @Test
    void deposit_onClosedAccount_returns409() throws Exception {
        when(accountService.deposit(eq(accountId), any(), any()))
                .thenThrow(new AccountNotActiveException(accountId, AccountStatus.CLOSED));

        AmountRequest request = amountRequest(new BigDecimal("10.00"));

        mockMvc.perform(post("/accounts/{accountId}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_ACTIVE"));
    }

    @Test
    void close_blockedAccount_returns409() throws Exception {
        when(accountService.closeAccount(accountId))
                .thenThrow(new AccountNotActiveException(accountId, AccountStatus.BLOCKED));
        mockMvc.perform(post("/accounts/{accountId}/close", accountId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_ACTIVE"));
    }

    @Test
    void openAccount_duplicateOperationId_returns409() throws Exception {
        when(accountService.openAccount(any(), any(), eq(operationId)))
                .thenThrow(new DuplicateOperationException(operationId));

        OpenAccountRequest request = openAccountRequest(
                BigDecimal.TEN,
                new AccountHolderRequest("Test User")
        );

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_OPERATION"));
    }
}