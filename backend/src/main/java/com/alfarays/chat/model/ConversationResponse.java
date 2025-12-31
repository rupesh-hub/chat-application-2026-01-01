package com.alfarays.chat.model;

import com.alfarays.user.model.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationResponse {
    private String id;
    private String name;
    private String type;
    private MessageResponse lastMessage;
    private Integer unreadCount;
    private UserResponse participant;
    private List<MessageResponse> messages;
    private String avatar;
}
