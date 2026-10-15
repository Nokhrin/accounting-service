package com.nokhrin.accounting.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokhrin.accounting.api.dto.AmountRequest;
import com.nokhrin.accounting.application.AccountService;
import com.nokhrin.accounting.domain.account.Account;
import com.nokhrin.accounting.domain.account.AccountHolder;
import com.nokhrin.accounting.domain.account.AccountNotFoundException;
import com.nokhrin.accounting.domain.account.AccountStatus;
import com.nokhrin.accounting.domain.operation.TransferResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AccountService accountService;

    @Test
    void transfer_validRequest_returnsTransferResponse() throws Exception {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        AccountHolder holder = new AccountHolder(UUID.randomUUID(), "Test");
        Account sourceAfter = new Account(sourceId, BigDecimal.ZERO, AccountStatus.ACTIVE, holder);
        Account targetAfter = new Account(targetId, new BigDecimal("100"), AccountStatus.ACTIVE, holder);
        TransferResult result = new TransferResult(UUID.randomUUID(), sourceAfter, targetAfter);

        when(accountService.transfer(sourceId, targetId, new BigDecimal("100")))
                .thenReturn(result);

        AmountRequest request = new AmountRequest(new BigDecimal("100"));

        mockMvc.perform(post("/accounts/{sourceId}/{targetId}/transfer", sourceId, targetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceAccount.id").value(sourceId.toString()))
                .andExpect(jsonPath("$.targetAccount.id").value(targetId.toString()));
    }

    @Test
    void transfer_accountNotFound_returns404() throws Exception {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(accountService.transfer(sourceId, targetId, new BigDecimal("100")))
                .thenThrow(new AccountNotFoundException(sourceId));

        AmountRequest request = new AmountRequest(new BigDecimal("100"));

        mockMvc.perform(post("/accounts/{sourceId}/{targetId}/transfer", sourceId, targetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));
    }
}