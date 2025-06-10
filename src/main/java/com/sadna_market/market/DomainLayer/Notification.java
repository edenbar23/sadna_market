package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
public class Notification {

    @Id
    @Column(name = "notification_id")
    private UUID notificationId;

    @Setter
    @Column(name = "recipient_username", nullable = false, length = 50)
    private String recipientUsername;

    @Setter
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Setter
    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Setter
    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Setter
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column(name = "related_store_id")
    private UUID relatedStoreId;

    @Setter
    @Column(name = "related_order_id")
    private UUID relatedOrderId;

    @Setter
    @Column(name = "related_product_id")
    private UUID relatedProductId;

    public Notification(String recipientUsername, String title, String message,
                        NotificationType type, UUID relatedStoreId, UUID relatedOrderId, UUID relatedProductId) {
        this.notificationId = UUID.randomUUID();
        this.recipientUsername = recipientUsername;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
        this.relatedStoreId = relatedStoreId;
        this.relatedOrderId = relatedOrderId;
        this.relatedProductId = relatedProductId;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return notificationId.equals(that.notificationId);
    }

    @Override
    public int hashCode() {
        return notificationId.hashCode();
    }
}