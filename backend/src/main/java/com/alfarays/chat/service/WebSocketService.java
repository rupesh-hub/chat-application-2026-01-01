package com.alfarays.chat.service;

import com.alfarays.chat.entity.Conversation;
import com.alfarays.chat.entity.Message;
import com.alfarays.chat.model.*;
import com.alfarays.chat.repository.ConversationRepository;
import com.alfarays.chat.repository.MessageRepository;
import com.alfarays.exceptions.AuthorizationException;
import com.alfarays.user.entity.User;
import com.alfarays.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, Integer> sessionTracker = new ConcurrentHashMap<>();

    @Transactional
    public void handleUserPresence(String userId, boolean isConnecting) {
        sessionTracker.compute(userId, (key, count) -> {
            int newCount = (count == null) ? 0 : count;
            if(isConnecting) newCount++;
            else newCount = Math.max(0, newCount - 1);

            // Transition logic
            if(isConnecting && newCount == 1) broadcastStatusChange(userId, "ONLINE");
            else if(!isConnecting && newCount == 0) broadcastStatusChange(userId, "OFFLINE");
            return newCount;
        });
    }

    private void broadcastStatusChange(String userId, String status) {
        userRepository.findByEmail(userId).ifPresent(user -> {
            if(status.equals("OFFLINE")) {
                if(!user.getStatus().equals("OFFLINE")) {
                    user.setStatus(status);
                    user.setLastSeen(LocalDateTime.now());
                    userRepository.save(user);
                }
            } else {
                if(!user.getStatus().equals("ONLINE")) {
                    user.setStatus(status);
                    userRepository.save(user);
                }
            }
        });

        // Notify all partners
        List<Conversation> conversations = conversationRepository
                .findActiveConversationsByUsername(userId, PageRequest.of(0, 1000));

        for(Conversation conversation : conversations) {
            String partnerEmail = userId.equals(conversation.getInitiator())
                    ? conversation.getParticipant()
                    : conversation.getInitiator();

            messagingTemplate.convertAndSendToUser(
                    partnerEmail,
                    "/queue/status",
                    new StatusNotification(userId, status, LocalDateTime.now().toString())
            );
        }
    }

    @Transactional(readOnly = true)
    public void syncPartnerStatuses(String requesterEmail) {
        List<Conversation> conversations = conversationRepository
                .findActiveConversationsByUsername(requesterEmail, PageRequest.of(0, 1000));

        for(Conversation conversation : conversations) {
            String partnerEmail = requesterEmail.equals(conversation.getInitiator())
                    ? conversation.getParticipant()
                    : conversation.getInitiator();

            // Check if partner is in our ACTIVE session map
            boolean isPartnerOnline = sessionTracker.getOrDefault(partnerEmail, 0) > 0;
            String status = isPartnerOnline ? "ONLINE" : "OFFLINE";

            messagingTemplate.convertAndSendToUser(
                    requesterEmail,
                    "/queue/status",
                    new StatusNotification(partnerEmail, status, LocalDateTime.now().toString())
            );
        }
    }

    @Transactional
    public void partnerStatus(String requesterEmail) {
        // 1. Update the requester to ONLINE in the database
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        requester.setStatus("ONLINE");
        userRepository.save(requester);

        // 2. Fetch all conversations where the requester is a participant
        List<Conversation> conversations = conversationRepository
                .findActiveConversationsByUsername(requesterEmail, PageRequest.of(0, 1000));

        for(Conversation conversation : conversations) {
            // 3. Identify the OTHER person
            String partnerEmail = requesterEmail.equals(conversation.getInitiator())
                    ? conversation.getParticipant()
                    : conversation.getInitiator();

            userRepository.findByEmail(partnerEmail).ifPresent(partner -> {

                // ACTION A: Tell the Partner that the Requester is now ONLINE
                messagingTemplate.convertAndSendToUser(
                        partnerEmail,
                        "/queue/status",
                        new StatusNotification(requesterEmail, "ONLINE", LocalDateTime.now().toString())
                );

                // ACTION B: Tell the Requester what this Partner's status is
                messagingTemplate.convertAndSendToUser(
                        requesterEmail,
                        "/queue/status",
                        new StatusNotification(
                                partnerEmail,
                                partner.getStatus(),
                                partner.getLastSeen() != null ? partner.getLastSeen().toString() : ""
                        )
                );
            });
        }
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
