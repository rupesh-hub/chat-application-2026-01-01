package com.alfarays.chat.service;

import com.alfarays.chat.model.ConversationResponse;
import com.alfarays.util.GlobalResponse;

import java.util.List;

public interface IConversationService {
    GlobalResponse<ConversationResponse> getOrCreateConversation(String initiator, String participant);

    GlobalResponse<List<ConversationResponse>> getUserConversations(String userId, int page, int size, String query);

    GlobalResponse<String> deleteConversation(String conversationId, String userId);

    GlobalResponse<ConversationResponse> getConversationById(String conversationId, String authenticatorUsername);
}
