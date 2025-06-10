package com.sadna_market.market.DomainLayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface INotificationRepository {
    void save(Notification notification);
    Optional<Notification> findById(UUID notificationId);
    List<Notification> findByRecipientUsername(String username);
    List<Notification> findUnreadByRecipientUsername(String username);
    void delete(UUID notificationId);
    void markAsRead(UUID notificationId);
    void markAllAsReadForUser(String username);
    int countUnreadForUser(String username);
}