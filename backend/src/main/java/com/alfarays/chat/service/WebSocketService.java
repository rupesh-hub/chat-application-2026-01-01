package com.alfarays.chat.service;

import com.alfarays.chat.entity.Conversation;
import com.alfarays.chat.entity.Message;
import com.alfarays.chat.model.*;
import com.alfarays.chat.repository.ConversationRepository;
import com.alfarays.chat.repository.MessageRepository;
import com.alfarays.exceptions.AuthorizationException;
import com.alfarays.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    // 🔑 session count per user
    private final Map<String, Integer> userSessionCounts = new ConcurrentHashMap<>();

    // 🔑 delayed OFFLINE tasks
    private final Map<String, ScheduledFuture<?>> offlineTasks = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    /* =========================
       SESSION MANAGEMENT (FIX)
       ========================= */

    public void incrementSession(String userId) {
        userSessionCounts.merge(userId, 1, Integer::sum);

        // ✅ cancel pending OFFLINE
        ScheduledFuture<?> task = offlineTasks.remove(userId);
        if (task != null) {
            task.cancel(false);
            log.info("Cancelled OFFLINE task for {}", userId);
        }

        log.info("Session++ {} -> {}", userId, userSessionCounts.get(userId));
    }

    public void decrementSession(String userId) {
        userSessionCounts.compute(userId,
                (k, v) -> (v == null || v <= 1) ? 0 : v - 1);

        log.info("Session-- {} -> {}", userId,
                userSessionCounts.getOrDefault(userId, 0));
    }

    /* =========================
       PRESENCE (FIXED)
       ========================= */

    @Transactional
    public void notifyUserStatus(String userId, String partnerId, boolean ignored) {
        int sessions = userSessionCounts.getOrDefault(userId, 0);

        if (sessions > 0) {
            // ✅ ONLINE immediately
            updateStatusInDbAndNotify(userId, partnerId, "ONLINE");
            return;
        }

        // ⏳ schedule OFFLINE only ONCE
        offlineTasks.computeIfAbsent(userId, u ->
                scheduler.schedule(() -> {
                    if (userSessionCounts.getOrDefault(userId, 0) == 0) {
                        updateStatusInDbAndNotify(userId, partnerId, "OFFLINE");
                    }
                    offlineTasks.remove(userId);
                }, 3, TimeUnit.SECONDS)
        );
    }

    private void updateStatusInDbAndNotify(
            String userId, String partnerId, String status) {

        userRepository.findByEmail(userId).ifPresent(user -> {
            user.setStatus(status);
            if ("OFFLINE".equals(status)) {
                user.setLastSeen(LocalDateTime.now());
            }
            userRepository.save(user);
            log.info("Persisted {} for {}", status, userId);
        });

        messagingTemplate.convertAndSendToUser(
                partnerId,
                "/queue/status",
                new StatusNotification(
                        userId,
                        status,
                        LocalDateTime.now().toString()
                )
        );
    }

    /* =========================
       ALL YOUR ORIGINAL METHODS
       ========================= */

    @Transactional
    public void sendPrivateMessage(String conversationId, String sender, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AuthorizationException("No conversation exists"));

        Message message = Message.builder()
                .conversation(conversation)
                .senderId(sender)
                .content(content)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        messageRepository.save(message);

        MessageResponse response = mapToResponse(message);

        String receiver = sender.equals(conversation.getInitiator())
                ? conversation.getParticipant()
                : conversation.getInitiator();

        messagingTemplate.convertAndSendToUser(
                receiver,
                "/queue/private-messages",
                response
        );

        messagingTemplate.convertAndSendToUser(
                sender,
                "/queue/message-sent",
                response
        );

        long unreadCount =
                messageRepository.countUnreadMessagesBySender(conversationId, receiver);

        messagingTemplate.convertAndSendToUser(
                receiver,
                "/queue/unread-count",
                new UnreadCountUpdate(conversationId, unreadCount)
        );
    }

    @Transactional
    public void notifyTyping(String conversationId, String userId, boolean isTyping) {
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId + "/typing",
                new TypingNotification(
                        userId,
                        isTyping,
                        LocalDateTime.now().toString()
                )
        );
    }

    @Transactional
    public void markConversationAsRead(String conversationId, String readerId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        messageRepository.markAsReadByConversationAndUserId(conversationId, readerId);

        String recipient =
                conv.getInitiator().equals(readerId)
                        ? conv.getParticipant()
                        : conv.getInitiator();

        messagingTemplate.convertAndSendToUser(
                recipient,
                "/queue/messages-read",
                new ReadReceipt(
                        conversationId,
                        readerId,
                        LocalDateTime.now().toString()
                )
        );
    }

    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .readAt(message.getReadAt())
                .isRead(message.getIsRead())
                .build();
    }
}
