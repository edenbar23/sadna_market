package com.sadna_market.market.DomainLayer;

public enum NotificationType {
    ORDER_RECEIVED,        // Store owner gets notification when customer places order
    STORE_CLOSED,         // Store personnel get notification when store is closed
    STORE_REOPENED,       // Store personnel get notification when store is reopened
    ROLE_REMOVED,         // User gets notification when their role is removed
    ROLE_ASSIGNED,        // User gets notification when they get a new role
    MESSAGE_RECEIVED,     // User gets notification when they receive a message
    VIOLATION_REPLY,      // User gets notification when admin replies to their report
    SYSTEM_ANNOUNCEMENT   // General system announcements
}