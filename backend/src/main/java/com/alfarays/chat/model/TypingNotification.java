package com.alfarays.chat.model;

public record TypingNotification(String userId, boolean isTyping, String timestamp) {
}