package com.alfarays.chat.service;

import com.alfarays.chat.entity.Conversation;
import com.alfarays.chat.entity.Message;
import com.alfarays.chat.model.ConversationResponse;
import com.alfarays.chat.model.MessageResponse;
import com.alfarays.chat.repository.ConversationRepository;
import com.alfarays.exceptions.AuthorizationException;
import com.alfarays.exceptions.ResourceNotFoundException;
import com.alfarays.user.model.UserResponse;
import com.alfarays.user.repository.UserRepository;
import com.alfarays.util.GlobalResponse;
import com.alfarays.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService implements IConversationService {

    private final ConversationRepository conversationRepository;
    private final IMessageService messageService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public GlobalResponse<ConversationResponse> getOrCreateConversation(String initiator, String participant) {

        // 1️⃣ Validate
        if(initiator == null || initiator.isBlank()) {
            log.warn("User not authenticated for conversation creation");
            throw new AuthorizationException("Please login to the system!");
        }

        if(participant == null || participant.isBlank()) {
            log.warn("Invalid recipient username provided");
            throw new AuthorizationException("Invalid recipient username provided");
        }

        if(initiator.equalsIgnoreCase(participant)) {
            throw new AuthorizationException("Cannot create conversation with yourself !");
        }

        // 2️⃣ Generate unique key
        String key = generateConversationKey(initiator, participant);

        // 3️⃣ Try to find existing conversation
        return conversationRepository
                .findByKey(key)
                .map(conversation -> {
                    log.info("Existing conversation found: {}", key);
                    return GlobalResponse.success(mapToResponse(conversation, initiator));
                })
                .orElseGet(() -> {
                    log.info("Creating new conversation: {}", key);

                    Conversation conversation = Conversation.builder()
                            .conversationKey(key)
                            .createdAt(LocalDateTime.now())
                            .initiator(initiator)
                            .participant(participant)
                            .isActive(true)
                            .build();

                    Conversation saved = conversationRepository.save(conversation);
                    return GlobalResponse.success(mapToResponse(saved, initiator));
                });
    }


    @Override
    @Transactional(readOnly = true)
    public GlobalResponse<List<ConversationResponse>> getUserConversations(
            String userId, int page, int size, String query) {

        if(userId == null || userId.isBlank()) {
            log.warn("Invalid userId for fetching conversations");
            throw new AuthorizationException("Invalid user ID");
        }

        Pageable pageable = PageRequest.of(page, size);

        try {
            List<Conversation> conversations =
                    conversationRepository.findUserConversationsFiltered(
                            userId,
                            query,
                            pageable
                    );

            List<ConversationResponse> responses = conversations.stream()
                    .map(c -> mapToResponse(c, userId))
                    .toList();

            return GlobalResponse.success(responses);

        } catch(Exception e) {
            log.error("Error fetching conversations for user: {}", userId, e);
            throw new AuthorizationException("Error fetching conversations");
        }
    }

    @Override
    @Transactional
    public GlobalResponse<String> deleteConversation(String conversationId, String userId) {
        if(conversationId == null || conversationId.isBlank() || userId == null || userId.isBlank()) {
            log.warn("Invalid parameters for deleteConversation");
            throw new RuntimeException("Invalid parameters");
        }

        log.debug("Deleting conversation: {} by user: {}", conversationId, userId);

        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

            if(!conversation.getInitiator().equals(userId) && !conversation.getParticipant().equals(userId)) {
                log.warn("User {} is not authorized to delete conversation {}", userId, conversationId);
                throw new RuntimeException("Not authorized to delete this conversation");
            }

            conversation.setIsActive(false);
            conversationRepository.save(conversation);
            log.info("Conversation {} deleted by user {}", conversationId, userId);

            return GlobalResponse.success("Conversation deleted successfully");
        } catch(ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch(Exception e) {
            log.error("Error deleting conversation", e);
            throw new RuntimeException("Internal Server Error");
        }
    }

    @Override
    public GlobalResponse<ConversationResponse> getConversationById(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
        return GlobalResponse.success(mapToResponse(conversation, userId));
    }

    private ConversationResponse mapToResponse(Conversation conversation, String currentUserId) {
        // 1. Identify both entities to access profile photos and names
        var initiatorUser = userRepository.findByEmail(conversation.getInitiator())
                .orElseThrow(() -> new AuthorizationException("Initiator not found."));

        var participantUser = userRepository.findByEmail(conversation.getParticipant())
                .orElseThrow(() -> new AuthorizationException("Participant not found."));

        // 2. Determine who the "Other User" is relative to the current logged-in user
        boolean isCurrentUserParticipant = currentUserId.equals(participantUser.getEmail());
        var otherUser = isCurrentUserParticipant ? initiatorUser : participantUser;

        // 3. Logic for Avatar Path
        String avatarPath = (otherUser.getProfile() != null)
                ? otherUser.getProfile().getPath()
                : "assets/images/default-avatar.png";

        // 4. Calculate Unread Message Count
        // This counts messages in this conversation where I am NOT the sender and I haven't read them yet
        long unreadCount = conversation.getMessages() != null
                ? conversation.getMessages().stream()
                .filter(m -> !m.getSenderId().equals(currentUserId)) // Messages not sent by me
                .filter(m -> !m.getIsRead())                        // Messages not yet read
                .count()
                : 0L;

        // 5. Extract Last Message Content
        String lastMessageContent = conversation.getMessages() != null && !conversation.getMessages().isEmpty()
                ? conversation.getMessages().stream()
                .max(Comparator.comparing(Message::getCreatedAt))
                .map(Message::getContent)
                .orElse(null)
                : null;

        // 6. Map Message List
        List<MessageResponse> messages = new ArrayList<>();
        if(null != conversation.getMessages()) {
            messages = conversation.getMessages().stream()
                    .map(message -> MessageResponse.builder()
                            .id(message.getId())
                            .readAt(message.getReadAt())
                            .senderId(message.getSenderId())
                            .isRead(message.getIsRead())
                            .conversationId(message.getConversation().getId())
                            .content(message.getContent())
                            .createdAt(message.getCreatedAt())
                            .build())
                    .sorted(Comparator.comparing(MessageResponse::getCreatedAt)) // Keep messages in order
                    .toList();
        }

        return ConversationResponse.builder()
                .id(conversation.getId())
                .name(name(otherUser.getFirstname(), otherUser.getLastname()))
                .avatar(avatarPath)
                .participant(
                        UserResponse.builder()
                                .lastSeen(otherUser.getLastLogin() != null ? otherUser.getLastLogin().toString() : LocalDateTime.now().toString())
                                .email(otherUser.getEmail())
                                .status("offline")
                                .profile(avatarPath)
                                .build()
                )
                .lastMessage(
                        MessageResponse.builder()
                                .content(lastMessageContent)
                                .createdAt(conversation.getLastMessageAt())
                                .build()
                )
                .messages(messages)
                .unreadCount((int) unreadCount)
                .build();
    }

    private String generateConversationKey(String user1, String user2) {
        return Stream.of(user1, user2)
                .map(String::toLowerCase)
                .sorted()
                .collect(Collectors.joining("_"));
    }

    private static String name(String firstname, String lastname) {
        return Util.capitalize(firstname) + " " + Util.capitalize(lastname);
    }
}