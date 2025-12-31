package com.alfarays.chat.model;

public record MessageReceivedNotification(String messageId, String senderId, String conversationId, String timestamp) {}