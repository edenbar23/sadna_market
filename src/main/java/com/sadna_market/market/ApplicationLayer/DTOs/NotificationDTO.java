package com.sadna_market.market.ApplicationLayer.DTOs;

import com.sadna_market.market.DomainLayer.NotificationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private UUID notificationId;
    private String recipientUsername;
    private String title;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;
    private UUID relatedStoreId;
    private UUID relatedOrderId;
    private UUID relatedProductId;
}