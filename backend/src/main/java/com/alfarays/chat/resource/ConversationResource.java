package com.alfarays.chat.resource;

import com.alfarays.chat.model.ConversationResponse;
import com.alfarays.chat.service.IConversationService;
import com.alfarays.exceptions.AuthorizationException;
import com.alfarays.util.GlobalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@Slf4j
public class ConversationResource {

    private final IConversationService conversationService;

    @PostMapping("/with/{participant}")
    public ResponseEntity<GlobalResponse<ConversationResponse>> getOrCreateConversation(@PathVariable String participant) {
        String initiator = extractUserId();

        log.info("User {} requesting conversation with {}", initiator, participant);
        return new ResponseEntity<>(conversationService.getOrCreateConversation(initiator, participant), HttpStatus.CREATED);
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<GlobalResponse<ConversationResponse>> getConversationById(
            @PathVariable String conversationId
    ) {
        return new ResponseEntity<>(
                conversationService.getConversationById(conversationId, extractUserId()),
                HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<ConversationResponse>>> loadConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query) {
        String userId = extractUserId();

        if(userId == null || userId.isBlank()) {
            log.warn("User not authenticated for fetching conversations");
            throw new AuthorizationException("User not authenticated for fetching conversations");
        }

        if(page < 0 || size <= 0 || size > 100) {
            log.warn("Invalid pagination parameters: page={}, size={}", page, size);
            throw new AuthorizationException(String.format("Invalid pagination parameters: page=%s, size=%s", page, size));
        }

        log.info("Fetching conversations for user: {}", userId);
        return new ResponseEntity<>(conversationService.getUserConversations(userId, page, size, query), HttpStatus.OK);
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<GlobalResponse<String>> deleteConversation(
            @PathVariable String conversationId) {
        String userId = extractUserId();

        if(userId == null || userId.isBlank()) {
            log.warn("User not authenticated for deleting conversation");
            throw new AuthorizationException("User not authenticated for deleting conversation");
        }

        if(conversationId == null || conversationId.isBlank()) {
            log.warn("Invalid conversationId provided");
            throw new AuthorizationException("Invalid conversationId provided");
        }

        log.info("User {} deleting conversation: {}", userId, conversationId);

        GlobalResponse<String> response = conversationService.deleteConversation(conversationId, userId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }

    private String extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return null;
    }
}
