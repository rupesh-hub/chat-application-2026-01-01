package com.alfarays.chat.repository;

import com.alfarays.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt DESC")
    Page<Message> findByConversationId(@Param("conversationId") String conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.isRead = false AND m.senderId != :userId")
    List<Message> findUnreadMessagesByConversationAndUserId(
            @Param("conversationId") String conversationId,
            @Param("userId") String userId
    );

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE m.conversation.id = :conversationId AND m.senderId != :userId AND m.isRead = false")
    void markAsReadByConversationAndUserId(@Param("conversationId") String conversationId, @Param("userId") String userId);

    @Query(
            """
            SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.isRead = false AND m.senderId != :readerId
           """
    )
    int countUnreadMessagesBySender(@Param("conversationId") String conversationId,
                                    @Param("readerId") String readerId);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.createdAt BETWEEN :startDate AND :endDate ORDER BY m.createdAt ASC")
    List<Message> findMessagesBetweenDates(
            @Param("conversationId") String conversationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
