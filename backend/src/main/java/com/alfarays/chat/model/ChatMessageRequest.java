package com.alfarays.chat.model;

public record ChatMessageRequest(String conversationId, String content) {
}