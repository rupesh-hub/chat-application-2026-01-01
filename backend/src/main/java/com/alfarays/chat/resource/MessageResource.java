package com.alfarays.chat.resource;

import com.alfarays.chat.model.MessageRequest;
import com.alfarays.chat.model.MessageResponse;
import com.alfarays.chat.service.IMessageService;
import com.alfarays.util.GlobalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageResource {

    /**
     * /messages/conversation/${conversationId}
     * /messages/conversation/{conversationId}/mark-read
     * /messages/conversation/{conversationId}/unread-count
     */

    private final IMessageService messageService;

    @PostMapping
    public ResponseEntity<GlobalResponse<MessageResponse>> send(@RequestBody MessageRequest request) {
        String sender = extractUserId();
        return ResponseEntity.ok(messageService.send(request, sender));
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<?> getConversationMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size) {
        String userId = extractUserId();

        if(userId == null || userId.isBlank()) {
            log.warn("User not authenticated for fetching messages");
            return null;
        }

        if(conversationId == null || conversationId.isBlank()) {
            log.warn("Invalid conversationId provided");
            return null;
        }

        if(page < 0 || size <= 0 || size > 1000) {
            log.warn("Invalid pagination parameters: page={}, size={}", page, size);
            return null;
        }

        log.info("Fetching messages for conversation: {} by user: {}", conversationId, userId);

        return new ResponseEntity<>(
                messageService.getConversationMessages(conversationId, userId, page, size), HttpStatus.OK
        );
    }

    @PutMapping("/conversation/{conversationId}/mark-read")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable String conversationId) {
        String userId = extractUserId();

        if(userId == null || userId.isBlank()) {
            log.warn("User not authenticated for marking messages as read");
            return null;
        }

        if(conversationId == null || conversationId.isBlank()) {
            log.warn("Invalid conversationId provided");
            return null;
        }

        log.info("Marking messages as read for conversation: {} by user: {}", conversationId, userId);

        messageService.markMessagesAsRead(conversationId, userId);
        return new ResponseEntity<>(GlobalResponse.success("Messages marked as read"), HttpStatus.OK);
    }


    private String extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return null;
    }
}
