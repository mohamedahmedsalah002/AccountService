package com.example.Account.Service.event.producer;

import com.example.Account.Service.event.model.AccountCreatedEvent;
import com.example.Account.Service.model.entity.Account;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String ACCOUNT_CREATED_TOPIC = "account.created";

    public void publishAccountCreated(Account account) {
        try {
            AccountCreatedEvent event = AccountCreatedEvent.from(account);
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ACCOUNT_CREATED_TOPIC, account.getAccountId().toString(), eventJson);
            log.info("Published ACCOUNT_CREATED event for accountId: {}", account.getAccountId());
        } catch (Exception e) {
            log.error("Failed to publish ACCOUNT_CREATED event for accountId: {}", account.getAccountId(), e);
        }
    }
}
