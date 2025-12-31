package com.alfarays.chat.model;

public record ConversationNotification(String conversationId, String otherUserId, String type, String timestamp) {}