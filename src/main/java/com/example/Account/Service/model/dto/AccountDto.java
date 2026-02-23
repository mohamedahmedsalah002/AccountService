package com.example.Account.Service.model.dto;

import com.example.Account.Service.model.entity.Account.AccountStatus;
import com.example.Account.Service.model.entity.Account.AccountType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {

    private Long accountId;
    private String accountNumber;
    private UUID userId;
    private BigDecimal balance;
    private AccountType accountType;
    private AccountStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
