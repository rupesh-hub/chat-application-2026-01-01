package com.alfarays.chat.model;

public record MessageRequest(
        String conversationId,
        String message
) {
}
