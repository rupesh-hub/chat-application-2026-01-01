package com.alfarays.chat.model;

public record UnreadCountUpdate(String conversationId, long unreadCount) {}