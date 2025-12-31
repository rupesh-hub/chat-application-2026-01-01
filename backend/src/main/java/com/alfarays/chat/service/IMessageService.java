package com.alfarays.chat.service;

import com.alfarays.chat.model.MessageRequest;
import com.alfarays.chat.model.MessageResponse;
import com.alfarays.util.GlobalResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IMessageService {
    GlobalResponse<MessageResponse> send(MessageRequest request, String senderId);
    GlobalResponse<List<MessageResponse>> getConversationMessages(String conversationId, String userId, int page, int size);
    void markMessagesAsRead(String conversationId, String userId);
}
