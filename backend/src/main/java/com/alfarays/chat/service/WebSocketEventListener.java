package com.alfarays.chat.service;

import com.alfarays.chat.entity.Conversation;
import com.alfarays.chat.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final WebSocketService webSocketService;
    private final ConversationRepository conversationRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        sha.getUser();

        String currentUserId = sha.getUser().getName();
        log.info("WebSocket Connection established for user: {}", currentUserId);

        // 1. Find all people this user has a conversation with
        List<Conversation> conversations = conversationRepository.findActiveConversationsByUsername(
                currentUserId, PageRequest.of(0, 100000)
        );

        // 2. Identify all unique partners (Initiators or Participants)
        Set<String> partnerIds = conversations.stream()
                .map(c -> c.getInitiator().equals(currentUserId) ? c.getParticipant() : c.getInitiator())
                .collect(Collectors.toSet());

        // 3. Notify all partners that CURRENT user is now ONLINE
        // We broadcast currentUserId's status TO each partner
        partnerIds.forEach(partnerId -> {
            webSocketService.notifyUserStatus(currentUserId, partnerId, true);
        });

        log.debug("Notified {} partners about user {} being ONLINE", partnerIds.size(), currentUserId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        if(sha.getUser() == null) return;

        String currentUserId = sha.getUser().getName();
        log.info("WebSocket Disconnected for user: {}", currentUserId);

        // 1. Find all people this user has a conversation with
        List<Conversation> conversations = conversationRepository.findActiveConversationsByUsername(
                currentUserId, PageRequest.of(0, 10000)
        );

        // 2. Identify all unique partners
        Set<String> partnerIds = conversations.stream()
                .map(c -> c.getInitiator().equals(currentUserId) ? c.getParticipant() : c.getInitiator())
                .collect(Collectors.toSet());

        // 3. Notify all partners that CURRENT user is now OFFLINE
        partnerIds.forEach(partnerId -> {
            webSocketService.notifyUserStatus(partnerId, currentUserId, false);
        });

        log.debug("Notified {} partners about user {} being OFFLINE", partnerIds.size(), currentUserId);
    }
}