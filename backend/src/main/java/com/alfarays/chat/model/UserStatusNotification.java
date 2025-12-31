package com.alfarays.chat.model;

public record UserStatusNotification(String userId, String status, Long timestamp) {
}