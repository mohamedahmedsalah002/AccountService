package com.example.Account.Service.controller;

import com.example.Account.Service.model.dto.AccountDto;
import com.example.Account.Service.model.dto.BalanceResponse;
import com.example.Account.Service.model.dto.CreateAccountRequest;
import com.example.Account.Service.model.dto.UpdateAccountRequest;
import com.example.Account.Service.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.info("Received request to create account for userId: {}", request.getUserId());
        AccountDto account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable Long id) {
        log.info("Received request to get account: {}", id);
        AccountDto account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountDto>> getAccountsByUser(@PathVariable UUID userId) {
        log.info("Received request to get accounts for userId: {}", userId);
        List<AccountDto> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable Long id, @Valid @RequestBody UpdateAccountRequest request) {
        log.info("Received request to update account: {}", id);
        AccountDto account = accountService.updateAccount(id, request);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeAccount(@PathVariable Long id) {
        log.info("Received request to close account: {}", id);
        accountService.closeAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long id) {
        log.info("Received request to get balance for account: {}", id);
        BalanceResponse balance = accountService.getBalance(id);
        return ResponseEntity.ok(balance);
    }
}
