package com.sadna_market.market.DomainLayer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Interface for all rating entities in the system
 */
public interface IRating {
    UUID getRatingId();
    String getUsername();
    int getRatingValue();
    LocalDateTime getTimestamp();
    boolean isUpdated();
}