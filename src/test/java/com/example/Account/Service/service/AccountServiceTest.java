package com.example.Account.Service.service;

import com.example.Account.Service.event.producer.AccountEventProducer;
import com.example.Account.Service.mapper.AccountMapper;
import com.example.Account.Service.model.dto.AccountDto;
import com.example.Account.Service.model.dto.CreateAccountRequest;
import com.example.Account.Service.model.entity.Account;
import com.example.Account.Service.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountEventProducer eventProducer;

    @InjectMocks
    private AccountService accountService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void createAccount_success() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .userId(userId)
                .accountType(Account.AccountType.CHECKING)
                .initialBalance(BigDecimal.valueOf(100))
                .build();

        Account savedAccount = Account.builder()
                .accountId(1L)
                .accountNumber("12345678901234567890")
                .userId(userId)
                .accountType(Account.AccountType.CHECKING)
                .balance(BigDecimal.valueOf(100))
                .status(Account.AccountStatus.ACTIVE)
                .build();

        when(accountRepository.countActiveAccountsByUserId(userId)).thenReturn(0L);
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        when(accountMapper.toDto(savedAccount)).thenReturn(AccountDto.builder()
                .accountId(1L)
                .accountNumber("12345678901234567890")
                .userId(userId)
                .balance(BigDecimal.valueOf(100))
                .build());

        AccountDto result = accountService.createAccount(request);

        assertThat(result).isNotNull();
        assertThat(result.getAccountId()).isEqualTo(1L);
        verify(accountRepository).save(any(Account.class));
        verify(eventProducer).publishAccountCreated(savedAccount);
    }

    @Test
    void createAccount_exceedsLimit_throwsException() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .userId(userId)
                .accountType(Account.AccountType.CHECKING)
                .build();

        when(accountRepository.countActiveAccountsByUserId(userId)).thenReturn(5L);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("maximum account limit");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_savingsWithLowBalance_throwsException() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .userId(userId)
                .accountType(Account.AccountType.SAVINGS)
                .initialBalance(BigDecimal.valueOf(5))
                .build();

        when(accountRepository.countActiveAccountsByUserId(userId)).thenReturn(0L);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minimum balance");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void closeAccount_withPositiveBalance_throwsException() {
        Account account = Account.builder()
                .accountId(1L)
                .balance(BigDecimal.valueOf(100))
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.closeAccount(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("positive balance");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void closeAccount_withZeroBalance_success() {
        Account account = Account.builder()
                .accountId(1L)
                .balance(BigDecimal.ZERO)
                .status(Account.AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.closeAccount(1L);

        verify(accountRepository).save(argThat(acc -> acc.getStatus() == Account.AccountStatus.CLOSED));
    }
}
