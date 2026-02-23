package com.example.Account.Service.service;

import com.example.Account.Service.event.producer.AccountEventProducer;
import com.example.Account.Service.mapper.AccountMapper;
import com.example.Account.Service.model.dto.AccountDto;
import com.example.Account.Service.model.dto.BalanceResponse;
import com.example.Account.Service.model.dto.CreateAccountRequest;
import com.example.Account.Service.model.dto.UpdateAccountRequest;
import com.example.Account.Service.model.entity.Account;
import com.example.Account.Service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AccountEventProducer eventProducer;

    private static final int MAX_ACCOUNTS_PER_USER = 5;
    private static final BigDecimal MINIMUM_BALANCE = BigDecimal.valueOf(10.0);

    @Transactional
    public AccountDto createAccount(CreateAccountRequest request) {
        log.info("Creating account for userId: {}, type: {}", request.getUserId(), request.getAccountType());

        // Validation: Check account limit
        long activeAccounts = accountRepository.countActiveAccountsByUserId(request.getUserId());
        if (activeAccounts >= MAX_ACCOUNTS_PER_USER) {
            throw new IllegalStateException("User has reached maximum account limit: " + MAX_ACCOUNTS_PER_USER);
        }

        // Validation: Check minimum balance for SAVINGS
        if (request.getAccountType() == Account.AccountType.SAVINGS) {
            BigDecimal initialBalance = request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO;
            if (initialBalance.compareTo(MINIMUM_BALANCE) < 0) {
                throw new IllegalArgumentException("Savings account requires minimum balance of " + MINIMUM_BALANCE);
            }
        }

        // Generate unique account number
        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .userId(request.getUserId())
                .accountType(request.getAccountType())
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .status(Account.AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Account created: accountId={}, accountNumber={}", saved.getAccountId(), saved.getAccountNumber());

        // Publish account.created event
        eventProducer.publishAccountCreated(saved);

        return accountMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountById(Long accountId) {
        log.info("Fetching account by id: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        return accountMapper.toDto(account);
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByUserId(UUID userId) {
        log.info("Fetching accounts for userId: {}", userId);
        List<Account> accounts = accountRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return accountMapper.toDtoList(accounts);
    }

    @Transactional
    public AccountDto updateAccount(Long accountId, UpdateAccountRequest request) {
        log.info("Updating account: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }
        if (request.getStatus() != null) {
            // Validation: Cannot reopen a closed account
            if (account.getStatus() == Account.AccountStatus.CLOSED && request.getStatus() != Account.AccountStatus.CLOSED) {
                throw new IllegalStateException("Cannot reopen a closed account");
            }
            account.setStatus(request.getStatus());
        }

        Account updated = accountRepository.save(account);
        log.info("Account updated: accountId={}", accountId);
        return accountMapper.toDto(updated);
    }

    @Transactional
    public void closeAccount(Long accountId) {
        log.info("Closing account: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        // Validation: Cannot close account with positive balance
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Cannot close account with positive balance. Current balance: " + account.getBalance());
        }

        account.setStatus(Account.AccountStatus.CLOSED);
        accountRepository.save(account);
        log.info("Account closed: accountId={}", accountId);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long accountId) {
        log.info("Fetching balance for account: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        return BalanceResponse.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .retrievedAt(LocalDateTime.now())
                .build();
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.format("%020d", new Random().nextLong(100000000000000000L, 999999999999999999L));
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
