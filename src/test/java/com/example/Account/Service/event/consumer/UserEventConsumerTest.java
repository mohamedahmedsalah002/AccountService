package com.example.Account.Service.event.consumer;

import com.example.Account.Service.model.dto.AccountDto;
import com.example.Account.Service.model.dto.CreateAccountRequest;
import com.example.Account.Service.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventConsumerTest {

    @Mock
    private AccountService accountService;

    private UserEventConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        consumer = new UserEventConsumer(objectMapper, accountService);
    }

    @Test
    void consumeUserCreated_createsDefaultAccount() throws Exception {
        UUID userId = UUID.randomUUID();
        String message = """
                {
                    "eventId": "550e8400-e29b-41d4-a716-446655440000",
                    "eventType": "USER_CREATED",
                    "timestamp": "2026-02-12T10:00:00",
                    "serviceName": "user-service",
                    "data": {
                        "userId": "%s",
                        "username": "alice",
                        "email": "alice@test.com"
                    }
                }
                """.formatted(userId);

        when(accountService.createAccount(any(CreateAccountRequest.class)))
                .thenReturn(AccountDto.builder().accountId(1L).build());

        consumer.consumeUserCreated(message);

        ArgumentCaptor<CreateAccountRequest> captor = ArgumentCaptor.forClass(CreateAccountRequest.class);
        verify(accountService, times(1)).createAccount(captor.capture());

        CreateAccountRequest captured = captor.getValue();
        assertThat(captured.getUserId()).isEqualTo(userId);
        assertThat(captured.getAccountType().name()).isEqualTo("CHECKING");
    }

    @Test
    void consumeUserCreated_handlesInvalidJsonGracefully() {
        consumer.consumeUserCreated("invalid-json");
        verify(accountService, never()).createAccount(any());
    }
}
