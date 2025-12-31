package com.alfarays.chat.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatUserInfo {
    private String userId;
    private String displayName;
    private Boolean isOnline;
    private LocalDateTime lastSeen;
}
