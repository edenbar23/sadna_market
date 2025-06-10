package com.sadna_market.market.InfrastructureLayer.Adapters;

import com.sadna_market.market.DomainLayer.INotificationRepository;
import com.sadna_market.market.DomainLayer.Notification;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.NotificationJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
@Profile({"dev", "prod", "default"})
public class NotificationJpaAdapter implements INotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    @Autowired
    public NotificationJpaAdapter(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Notification notification) {
        jpaRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(UUID notificationId) {
        return jpaRepository.findById(notificationId);
    }

    @Override
    public List<Notification> findByRecipientUsername(String username) {
        return jpaRepository.findByRecipientUsernameOrderByCreatedAtDesc(username);
    }

    @Override
    public List<Notification> findUnreadByRecipientUsername(String username) {
        return jpaRepository.findByRecipientUsernameAndIsReadFalseOrderByCreatedAtDesc(username);
    }

    @Override
    public void delete(UUID notificationId) {
        jpaRepository.deleteById(notificationId);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId) {
        jpaRepository.markAsRead(notificationId);
    }

    @Override
    @Transactional
    public void markAllAsReadForUser(String username) {
        jpaRepository.markAllAsReadForUser(username);
    }

    @Override
    public int countUnreadForUser(String username) {
        return jpaRepository.countUnreadByRecipientUsername(username);
    }
}