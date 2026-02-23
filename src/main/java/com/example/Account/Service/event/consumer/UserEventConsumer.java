package com.example.Account.Service.event.consumer;

import com.example.Account.Service.event.model.CreateUserEvent;
import com.example.Account.Service.model.dto.CreateAccountRequest;
import com.example.Account.Service.model.entity.Account;
import com.example.Account.Service.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final ObjectMapper objectMapper;
    private final AccountService accountService;

    @KafkaListener(topics = "user.created", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserCreated(String message) {
        try {
            CreateUserEvent event = objectMapper.readValue(message, CreateUserEvent.class);
            log.info("Received user.created event: userId={}", event.getData().getUserId());

            // Auto-create a default CHECKING account for the new user
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .userId(event.getData().getUserId())
                    .accountType(Account.AccountType.CHECKING)
                    .initialBalance(BigDecimal.ZERO)
                    .build();

            accountService.createAccount(request);
            log.info("Auto-created default account for new user: userId={}", event.getData().getUserId());
        } catch (Exception e) {
            log.error("Failed to process user.created event", e);
        }
    }
}
