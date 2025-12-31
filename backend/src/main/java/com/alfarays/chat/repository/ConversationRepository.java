package com.alfarays.chat.repository;

import com.alfarays.chat.entity.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @Query("SELECT c FROM Conversation c WHERE (c.initiator = :username OR c.participant = :username) AND c.isActive = true")
    List<Conversation> findActiveConversationsByUsername(@Param("username") String username, Pageable pageable);

    @Query("""
        SELECT c FROM Conversation c
        WHERE c.isActive = true
          AND (c.initiator = :userId OR c.participant = :userId)
          AND (
                :query IS NULL OR :query = '' OR
                LOWER(
                    CASE 
                        WHEN c.initiator = :userId THEN c.participant
                        ELSE c.initiator
                    END
                ) LIKE LOWER(CONCAT('%', :query, '%'))
          )
        ORDER BY c.lastMessageAt DESC
    """)
    List<Conversation> findUserConversationsFiltered(
            @Param("userId") String userId,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("SELECT c FROM Conversation c WHERE c.conversationKey = :key")
    Optional<Conversation> findByKey(@Param("key") String key);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Conversation c WHERE c.conversationKey = :key")
    Boolean existsByKey(@Param("key") String key);
}
