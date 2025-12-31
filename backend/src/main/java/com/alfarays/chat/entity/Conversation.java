package com.alfarays.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "_conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, name="conversation_key")
    private String conversationKey;

    @Column(nullable = false, name="initiator")
    private String initiator;

    @Column(nullable = false, name = "participant")
    private String participant;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastMessageAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Message> messages = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if(createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
