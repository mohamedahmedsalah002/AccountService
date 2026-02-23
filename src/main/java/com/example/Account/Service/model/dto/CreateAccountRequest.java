package com.example.Account.Service.model.dto;

import com.example.Account.Service.model.entity.Account.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @DecimalMin(value = "0.0", message = "Initial balance must be non-negative")
    private BigDecimal initialBalance;
}
