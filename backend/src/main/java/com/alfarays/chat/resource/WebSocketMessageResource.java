package com.alfarays.chat.resource;

import com.alfarays.chat.model.ChatMessageRequest;
import com.alfarays.chat.model.MarkReadRequest;
import com.alfarays.chat.model.TypingIndicatorRequest;
import com.alfarays.chat.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageResource {

    private final WebSocketService webSocketService;

    @MessageMapping("/chat.sendMessage")
    public void handleChatMessage(@Payload ChatMessageRequest request, Principal principal) {
        if(principal == null) {
            log.warn("Received message from unauthenticated user");
            return;
        }

        String sender = principal.getName();
        log.debug("Received chat message from user: {}", sender);
        String sanitizedContent = HtmlUtils.htmlEscape(request.content());

        webSocketService.sendPrivateMessage(
                request.conversationId(),
                sender,
                sanitizedContent
        );
    }

    @MessageMapping("/chat.typing")
    public void handleTypingIndicator(@Payload TypingIndicatorRequest indicator, Principal principal) {
        if(principal == null) {
            log.warn("Received typing indicator from unauthenticated user");
            return;
        }

        String userId = principal.getName();
        log.debug("Typing indicator from user: {} in conversation: {}", userId, indicator.conversationId());
        webSocketService.notifyTyping(indicator.conversationId(), userId, indicator.isTyping());
    }

    @MessageMapping("/chat.markRead")
    public void handleMarkMessageAsRead(@Payload MarkReadRequest request, Principal principal) {
        if(principal == null || request.conversationId() == null) {
            log.warn("Invalid markAsRead attempt");
            return;
        }

        String reader = principal.getName();
        log.debug("User {} marked conversation {} as read", reader, request.conversationId());

        // Delegate to service (which updates DB and sends WebSocket notification to partner)
        webSocketService.markConversationAsRead(request.conversationId(), reader);
    }

}
