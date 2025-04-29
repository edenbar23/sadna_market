package com.sadna_market.market.DomainLayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IReportRepository {

    /**
     * Saves a message to the repository
     *
     * @param report The message to save
     * @return The saved message
     */
    boolean save(Report report);

    /**
     * Deletes a message by its ID
     *
     * @param reportId The message ID to delete
     * @return true if the message was deleted successfully
     */
    boolean deleteById(UUID reportId);

    /**
     * Updates a message in the repository
     *
     * @param report The message to update
     * @return The updated message
     */
    boolean update(Report report);

    /**
     * Finds a message by its ID
     *
     * @param reportId The message ID to look for
     * @return Optional containing the message if found
     */
    Optional<Report> findById(UUID reportId);

    /**
     * Finds all messages sent by a specific user
     *
     * @param username The username of the sender
     * @return List of messages sent by the user
     */
    List<Report> findBySender(String username);


}
