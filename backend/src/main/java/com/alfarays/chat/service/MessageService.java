package com.alfarays.chat.service;

import com.alfarays.chat.entity.Conversation;
import com.alfarays.chat.entity.Message;
import com.alfarays.chat.model.MessageRequest;
import com.alfarays.chat.model.MessageResponse;
import com.alfarays.chat.repository.ConversationRepository;
import com.alfarays.chat.repository.MessageRepository;
import com.alfarays.exceptions.AuthorizationException;
import com.alfarays.util.GlobalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService implements IMessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Override
    @Transactional(readOnly = true)
    public GlobalResponse<List<MessageResponse>> getConversationMessages(String conversationId, String userId, int page, int size) {
        if(conversationId == null || conversationId.isBlank()) {
            log.warn("Invalid conversationId: {}", conversationId);
            throw new AuthorizationException("");
        }

        log.debug("Fetching messages for conversation: {} by user: {}", conversationId, userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return GlobalResponse.success(messageRepository.findByConversationId(conversationId, pageable)
                .map(this::mapToResponse)
                .getContent()
        );
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String conversationId, String userId) {
        if(conversationId == null || conversationId.isBlank() || userId == null || userId.isBlank()) {
            log.warn("Invalid parameters for markMessagesAsRead");
            return;
        }

        log.debug("Marking messages as read for conversation: {} by user: {}", conversationId, userId);
        try {
            messageRepository.markAsReadByConversationAndUserId(conversationId, userId);
        } catch(Exception e) {
            log.error("Error marking messages as read for conversation: {}", conversationId, e);
        }
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

    @Override
    public GlobalResponse<MessageResponse> send(MessageRequest request, String senderId) {

        if(request.message() == null || request.message().isBlank())
            throw new IllegalArgumentException("Message cannot be empty");

        // 1️⃣ Find or create conversation
        Conversation conversation = conversationRepository
                .findById(request.conversationId())
                .orElseThrow(() -> new AuthorizationException("No conversation exists!"));

        // 2️⃣ Create message
        Message message = Message.builder()
                .conversation(conversation)
                .senderId(senderId)
                .content(request.message())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);

        // 3️⃣ Update conversation metadata
        conversation.setLastMessageAt(LocalDateTime.now());

        // 4️⃣ Map response
        return GlobalResponse.success(mapToResponse(saved));
    }


}
