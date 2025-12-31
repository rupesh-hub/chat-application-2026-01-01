package com.alfarays.chat.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private String id;
    private String conversationId;
    private String senderId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private Boolean isRead;
}
