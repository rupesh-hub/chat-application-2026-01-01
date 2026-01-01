package com.alfarays.chat.service;

import com.alfarays.chat.repository.ConversationRepository;
import com.alfarays.chat.service.WebSocketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final WebSocketService webSocketService;
    private final ConversationRepository conversationRepository;

    @EventListener
    @Transactional
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        String userId = StompHeaderAccessor.wrap(event.getMessage())
                .getUser().getName();

        webSocketService.incrementSession(userId);

        conversationRepository
                .findActiveConversationsByUsername(userId, PageRequest.of(0, 100000))
                .forEach(c -> {
                    String partner = c.getInitiator().equals(userId)
                            ? c.getParticipant()
                            : c.getInitiator();
                    webSocketService.notifyUserStatus(userId, partner, true);
                });
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String userId = StompHeaderAccessor.wrap(event.getMessage())
                .getUser().getName();

        webSocketService.decrementSession(userId);

        conversationRepository
                .findActiveConversationsByUsername(userId, PageRequest.of(0, 100000))
                .forEach(c -> {
                    String partner = c.getInitiator().equals(userId)
                            ? c.getParticipant()
                            : c.getInitiator();
                    webSocketService.notifyUserStatus(userId, partner, false);
                });
    }
}
