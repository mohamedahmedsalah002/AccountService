package com.example.Account.Service.event.model;

import com.example.Account.Service.model.entity.Account;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreatedEvent {

    private UUID eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String serviceName;
    private AccountEventData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountEventData {
        private Long accountId;
        private String accountNumber;
        private UUID userId;
        private String accountType;
        private BigDecimal balance;
        private LocalDateTime createdAt;
    }

    public static AccountCreatedEvent from(Account account) {
        return AccountCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("ACCOUNT_CREATED")
                .timestamp(LocalDateTime.now())
                .serviceName("account-service")
                .data(AccountEventData.builder()
                        .accountId(account.getAccountId())
                        .accountNumber(account.getAccountNumber())
                        .userId(account.getUserId())
                        .accountType(account.getAccountType().name())
                        .balance(account.getBalance())
                        .createdAt(account.getCreatedAt())
                        .build())
                .build();
    }
}
