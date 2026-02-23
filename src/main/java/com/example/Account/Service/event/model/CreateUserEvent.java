package com.example.Account.Service.event.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserEvent {

    private UUID eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String serviceName;
    private UserEventData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserEventData {
        private UUID userId;
        private String username;
        private String email;
    }
}
