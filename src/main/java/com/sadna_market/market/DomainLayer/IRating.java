package com.sadna_market.market.DomainLayer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Interface for all rating entities in the system
 */
public interface IRating {
    UUID getRatingId();
    UUID getUserId();
    String getUsername();
    int getRatingValue();
    LocalDateTime getTimestamp();
    String getComment();
    boolean isUpdated();
}