package com.sadna_market.market.InfrastructureLayer.JpaRepos;

import com.sadna_market.market.DomainLayer.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageJpaRepository extends JpaRepository<Message, UUID> {

    // Find messages by sender username
    List<Message> findBySenderUsername(String senderUsername);

    // Find messages by store ID
    List<Message> findByStoreId(UUID storeId);

    // Find messages by both sender and store
    List<Message> findBySenderUsernameAndStoreId(String senderUsername, UUID storeId);

    // Find unread messages for a store
    List<Message> findByStoreIdAndIsReadFalse(UUID storeId);

    // Find unanswered messages for a store (where reply is null or empty)
    @Query("SELECT m FROM Message m WHERE m.storeId = :storeId AND (m.reply IS NULL OR m.reply = '')")
    List<Message> findUnansweredMessagesByStore(@Param("storeId") UUID storeId);

    // Bulk update to mark all store messages as read
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.storeId = :storeId AND m.isRead = false")
    int markAllStoreMessagesAsRead(@Param("storeId") UUID storeId);

    // Count unread messages for a store
    int countByStoreIdAndIsReadFalse(UUID storeId);

    // Find messages ordered by timestamp (newest first)
    List<Message> findByStoreIdOrderByTimestampDesc(UUID storeId);

    // Find messages by sender ordered by timestamp
    List<Message> findBySenderUsernameOrderByTimestampDesc(String senderUsername);
}
