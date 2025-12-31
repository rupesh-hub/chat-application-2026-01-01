package com.alfarays.chat.service;

import com.alfarays.chat.entity.Conversation;
import com.alfarays.chat.entity.Message;
import com.alfarays.chat.model.*;
import com.alfarays.chat.repository.ConversationRepository;
import com.alfarays.chat.repository.MessageRepository;
import com.alfarays.exceptions.AuthorizationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Transactional
    public void sendGroupMessage(String conversationId, String senderId, String content) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        Message message = Message.builder()
                .conversation(conversation)
                .senderId(senderId)
                .content(content)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        messageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        MessageResponse response = mapToResponse(message);

        // 🔊 Broadcast to conversation
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                response
        );

        // ✅ Sender ACK (optional)
        messagingTemplate.convertAndSendToUser(
                senderId,
                "/queue/message-sent",
                response
        );
    }

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

        // 🔥 Determine receiver dynamically
        String receiver = sender.equals(conversation.getInitiator()) ? conversation.getParticipant() : conversation.getInitiator();

        // 📩 Send ONLY to receiver
        System.out.println("SEND MESSAGE TO : " + receiver);
        messagingTemplate.convertAndSendToUser(
                receiver,
                "/queue/private-messages",
                response
        );

        // ✅ Sender ACK
        System.out.println("ACK MESSAGE TO : " + sender);
        messagingTemplate.convertAndSendToUser(
                sender,
                "/queue/message-sent",
                response
        );

        // 3. 🔥 Unread Count Logic
        // Fetch count of messages where conversation is X, isRead is false, and sender is NOT the receiver
        long unreadCount = messageRepository.countUnreadMessagesBySender(conversationId, receiver);

        // Create a simple wrapper to send both the ID and the Count
        UnreadCountUpdate payload = new UnreadCountUpdate(conversationId, unreadCount);

        messagingTemplate.convertAndSendToUser(
                receiver,
                "/queue/unread-count",
                payload
        );
    }


    @Transactional
    public void notifyUserStatus(String userId, String partnerId, boolean isOnline) {
        // Validate both IDs
        if(userId == null || userId.isBlank() || partnerId == null || partnerId.isBlank()) {
            log.warn("Invalid IDs for status notification. Subject: {}, Recipient: {}", userId, partnerId);
            return;
        }

        log.debug("Sending status of {} to {}: {}", userId, partnerId, isOnline ? "ONLINE" : "OFFLINE");

        try {
            messagingTemplate.convertAndSendToUser(
                    partnerId,
                    "/queue/status",
                    new StatusNotification(
                            userId,
                            isOnline ? "ONLINE" : "OFFLINE",
                            LocalDateTime.now().toString()
                    )
            );
        } catch(Exception e) {
            log.error("Failed to notify user {} about status of {}", partnerId, userId, e);
        }
    }

    @Transactional
    public void notifyTyping(String conversationId, String userId, boolean isTyping) {
        if(conversationId == null || conversationId.isBlank() || userId == null || userId.isBlank()) {
            log.warn("Invalid parameters for typing notification");
            return;
        }

        log.debug("User {} typing notification in conversation {}: {}", userId, conversationId, isTyping);
        try {
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversationId + "/typing",
                    new TypingNotification(userId, isTyping, LocalDateTime.now().toString())
            );
        } catch(Exception e) {
            log.error("Error sending typing notification for conversation: {}", conversationId, e);
        }
    }

    @Transactional
    public void markConversationAsRead(String conversationId, String readerId) {
        // 1. Find the other participant to notify them
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // 2. Update messages in DB: Set isSeen = true where conversation matches and sender is NOT the reader
        messageRepository.markAsReadByConversationAndUserId(conversationId, readerId);

        String recipientToNotify = conv.getInitiator().equals(readerId)
                ? conv.getParticipant()
                : conv.getInitiator();

        // 3. Notify the original sender that their messages were read
        messagingTemplate.convertAndSendToUser(
                recipientToNotify,
                "/queue/messages-read",
                new ReadReceipt(conversationId, readerId, LocalDateTime.now().toString())
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
