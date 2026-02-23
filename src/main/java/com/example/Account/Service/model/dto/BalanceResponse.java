package com.example.Account.Service.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {

    private Long accountId;
    private String accountNumber;
    private BigDecimal balance;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime retrievedAt;
}
