package com.sadna_market.market.InfrastructureLayer.InMemoryRepos;

import com.sadna_market.market.DomainLayer.INotificationRepository;
import com.sadna_market.market.DomainLayer.Notification;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@Profile("test") // Use this repository only in test profile
public class InMemoryNotificationRepository implements INotificationRepository {

    private final Map<UUID, Notification> notifications = new ConcurrentHashMap<>();

    @Override
    public void save(Notification notification) {
        notifications.put(notification.getNotificationId(), notification);
    }

    @Override
    public Optional<Notification> findById(UUID notificationId) {
        return Optional.ofNullable(notifications.get(notificationId));
    }

    @Override
    public List<Notification> findByRecipientUsername(String username) {
        return notifications.values().stream()
                .filter(n -> n.getRecipientUsername().equals(username))
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findUnreadByRecipientUsername(String username) {
        return notifications.values().stream()
                .filter(n -> n.getRecipientUsername().equals(username) && !n.isRead())
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID notificationId) {
        notifications.remove(notificationId);
    }

    @Override
    public void markAsRead(UUID notificationId) {
        Notification notification = notifications.get(notificationId);
        if (notification != null) {
            notification.markAsRead();
        }
    }

    @Override
    public void markAllAsReadForUser(String username) {
        notifications.values().stream()
                .filter(n -> n.getRecipientUsername().equals(username) && !n.isRead())
                .forEach(Notification::markAsRead);
    }

    @Override
    public int countUnreadForUser(String username) {
        return (int) notifications.values().stream()
                .filter(n -> n.getRecipientUsername().equals(username) && !n.isRead())
                .count();
    }
}