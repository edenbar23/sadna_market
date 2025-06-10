package com.sadna_market.market.InfrastructureLayer.JpaRepos;

import com.sadna_market.market.DomainLayer.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientUsernameOrderByCreatedAtDesc(String recipientUsername);

    List<Notification> findByRecipientUsernameAndIsReadFalseOrderByCreatedAtDesc(String recipientUsername);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientUsername = :username AND n.isRead = false")
    int countUnreadByRecipientUsername(@Param("username") String username);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.notificationId = :notificationId")
    void markAsRead(@Param("notificationId") UUID notificationId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientUsername = :username AND n.isRead = false")
    void markAllAsReadForUser(@Param("username") String username);
}