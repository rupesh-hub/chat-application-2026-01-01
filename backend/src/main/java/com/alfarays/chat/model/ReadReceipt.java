package com.alfarays.chat.model;

public record ReadReceipt(String conversationId, String readerId, String timestamp) {
}
