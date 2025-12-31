package com.alfarays.chat.model;

public record TypingIndicatorRequest(String conversationId, boolean isTyping) {
}
