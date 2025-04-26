package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.DomainLayer.IStoreRepository;
import com.sadna_market.market.DomainLayer.Store;
import com.sadna_market.market.DomainLayer.StoreFounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for store-related operations in the market system.
 * Handles store creation, management, and queries.
 */
@Service
public class StoreService {
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);
    
    private final IStoreRepository storeRepository;
    
    /**
     * Constructor for StoreService
     * 
     * @param storeRepository Repository for store data
     */
    public StoreService(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
        logger.info("StoreService initialized");
    }
    
    /**
     * Opens a new store with the given details
     * 
     * @param storeRequest The store creation request containing store details
     * @return The UUID of the newly created store
     * @throws IllegalArgumentException if the store name already exists or request is invalid
     */
    public UUID openStore(StoreRequest storeRequest) {
        String founderUsername = storeRequest.getFounderUsername();
        logger.info("Opening new store with name: {} for founder: {}", 
                    storeRequest.getStoreName(), founderUsername);
        
        // Validate inputs
        if (founderUsername == null || founderUsername.isEmpty()) {
            logger.error("Founder username cannot be null or empty");
            throw new IllegalArgumentException("Founder username cannot be null or empty");
        }
        
        if (storeRequest == null || storeRequest.getStoreName() == null || storeRequest.getStoreName().isEmpty()) {
            logger.error("Store name cannot be null or empty");
            throw new IllegalArgumentException("Store name cannot be null or empty");
        }
        
        // Check if store name already exists
        Optional<Store> existingStore = storeRepository.findByName(storeRequest.getStoreName());
        if (existingStore.isPresent()) {
            logger.error("Store with name {} already exists", storeRequest.getStoreName());
            throw new IllegalArgumentException("Store with this name already exists");
        }
        
        // Create the store
        UUID storeId = storeRepository.createStore(
            founderUsername,
            storeRequest.getStoreName(),
            storeRequest.getAddress(),
            storeRequest.getEmail(),
            storeRequest.getPhoneNumber()
        );
        
        logger.info("Store created successfully with ID: {}", storeId);
        return storeId;
    }
    
    /**
     * Gets information about a store
     * 
     * @param storeId The UUID of the store
     * @return StoreDTO with store information
     * @throws IllegalArgumentException if the store doesn't exist
     */
    public StoreDTO getStoreInfo(UUID storeId) {
        logger.info("Getting store info for store ID: {}", storeId);
        
        Optional<Store> storeOpt = storeRepository.findById(storeId);
        if (!storeOpt.isPresent()) {
            logger.error("Store with ID {} not found", storeId);
            throw new IllegalArgumentException("Store not found");
        }
        
        Store store = storeOpt.get();
        
        // Convert to DTO
        StoreDTO storeDTO = new StoreDTO(
            store.getStoreId(),
            store.getName(),
            store.getDescription(),
            store.isActive(),
            store.getFounder().getUsername(),
            storeRepository.getStoreOwners(storeId),
            storeRepository.getStoreManagers(storeId)
        );
        
        logger.info("Returning store info for store: {}", store.getName());
        return storeDTO;
    }
    
    /**
     * Gets all stores in the system
     * 
     * @return List of StoreDTO objects
     */
    public List<StoreDTO> getAllStores() {
        logger.info("Getting all stores");
        
        List<Store> stores = storeRepository.findAll();
        List<StoreDTO> storeDTOs = new ArrayList<>();
        
        for (Store store : stores) {
            if (store.isActive()) {  // Only return active stores
                StoreDTO storeDTO = new StoreDTO(
                    store.getStoreId(),
                    store.getName(),
                    store.getDescription(),
                    true,
                    store.getFounder().getUsername(),
                    storeRepository.getStoreOwners(store.getStoreId()),
                    storeRepository.getStoreManagers(store.getStoreId())
                );
                storeDTOs.add(storeDTO);
            }
        }
        
        logger.info("Returning {} active stores", storeDTOs.size());
        return storeDTOs;
    }
    
    /**
     * Closes a store
     * 
     * @param storeId The UUID of the store to close
     * @param username The username of the user trying to close the store
     * @throws IllegalArgumentException if the store doesn't exist or user isn't authorized
     */
    public void closeStore(UUID storeId, String username) {
        logger.info("Attempting to close store with ID: {} by user: {}", storeId, username);
        
        // Check if store exists
        if (!storeRepository.exists(storeId)) {
            logger.error("Store with ID {} not found", storeId);
            throw new IllegalArgumentException("Store not found");
        }
        
        // Check if user is the founder or a system admin
        String founder = storeRepository.getStoreFounder(storeId);
        boolean isFounder = founder != null && founder.equals(username);
        
        if (!isFounder) {
            logger.error("User {} is not authorized to close store {}", username, storeId);
            throw new IllegalArgumentException("Only the store founder can close the store");
        }
        
        // Close the store
        storeRepository.updateStoreStatus(storeId, false);
        logger.info("Store with ID: {} closed successfully by user: {}", storeId, username);
        
        // Note: In a real implementation, we would send notifications to store owners and managers
        // This is mentioned in the requirements but is not implemented in version 1
    }
    
    /**
     * Reopens a closed store
     * 
     * @param storeId The UUID of the store to reopen
     * @param username The username of the user trying to reopen the store
     * @throws IllegalArgumentException if the store doesn't exist or user isn't authorized
     */
    public void reopenStore(UUID storeId, String username) {
        logger.info("Attempting to reopen store with ID: {} by user: {}", storeId, username);
        
        // Check if store exists
        if (!storeRepository.exists(storeId)) {
            logger.error("Store with ID {} not found", storeId);
            throw new IllegalArgumentException("Store not found");
        }
        
        // Check if user is the founder
        String founder = storeRepository.getStoreFounder(storeId);
        boolean isFounder = founder != null && founder.equals(username);
        
        if (!isFounder) {
            logger.error("User {} is not authorized to reopen store {}", username, storeId);
            throw new IllegalArgumentException("Only the store founder can reopen the store");
        }
        
        // Reopen the store
        storeRepository.updateStoreStatus(storeId, true);
        logger.info("Store with ID: {} reopened successfully by user: {}", storeId, username);
        
        // Note: In a real implementation, we would send notifications to store owners and managers
    }
    
    /**
     * Adds a store owner
     * 
     * @param storeId The UUID of the store
     * @param currentOwnerUsername The username of the current owner
     * @param newOwnerUsername The username of the new owner
     * @throws IllegalArgumentException if store doesn't exist or users aren't valid
     */
    public void addStoreOwner(UUID storeId, String currentOwnerUsername, String newOwnerUsername) {
        logger.info("Adding store owner {} to store {} by {}", 
                    newOwnerUsername, storeId, currentOwnerUsername);
        
        // Check if store exists
        if (!storeRepository.exists(storeId)) {
            logger.error("Store with ID {} not found", storeId);
            throw new IllegalArgumentException("Store not found");
        }
        
        // Check if current user is an owner
        if (!storeRepository.isOwner(storeId, currentOwnerUsername)) {
            logger.error("User {} is not a store owner", currentOwnerUsername);
            throw new IllegalArgumentException("Only store owners can add new owners");
        }
        
        // Check if new owner is already an owner
        if (storeRepository.isOwner(storeId, newOwnerUsername)) {
            logger.error("User {} is already a store owner", newOwnerUsername);
            throw new IllegalArgumentException("User is already a store owner");
        }
        
        // Check if new owner is already a manager
        if (storeRepository.isManager(storeId, newOwnerUsername)) {
            logger.error("User {} is already a store manager", newOwnerUsername);
            throw new IllegalArgumentException("User is already a store manager");
        }
        
        // Add the new owner
        storeRepository.addOwner(storeId, newOwnerUsername);
        logger.info("Added {} as owner to store {}", newOwnerUsername, storeId);
    }
    
    /**
     * Removes a store owner
     * 
     * @param storeId The UUID of the store
     * @param currentOwnerUsername The username of the current owner
     * @param ownerToRemoveUsername The username of the owner to remove
     * @throws IllegalArgumentException if store doesn't exist or users aren't valid
     */
    public void removeStoreOwner(UUID storeId, String currentOwnerUsername, String ownerToRemoveUsername) {
        logger.info("Removing store owner {} from store {} by {}", 
                    ownerToRemoveUsername, storeId, currentOwnerUsername);
        
        // Check if store exists
        if (!storeRepository.exists(storeId)) {
            logger.error("Store with ID {} not found", storeId);
            throw new IllegalArgumentException("Store not found");
        }
        
        // Check if current user is an owner
        if (!storeRepository.isOwner(storeId, currentOwnerUsername)) {
            logger.error("User {} is not a store owner", currentOwnerUsername);
            throw new IllegalArgumentException("Only store owners can remove owners");
        }
        
        // Check if owner to remove is an owner
        if (!storeRepository.isOwner(storeId, ownerToRemoveUsername)) {
            logger.error("User {} is not a store owner", ownerToRemoveUsername);
            throw new IllegalArgumentException("User is not a store owner");
        }
        
        // Check if trying to remove founder
        String founder = storeRepository.getStoreFounder(storeId);
        if (founder != null && founder.equals(ownerToRemoveUsername)) {
            logger.error("Cannot remove the founder of the store");
            throw new IllegalArgumentException("Cannot remove the founder of the store");
        }
        
        // Remove the owner
        storeRepository.removeOwner(storeId, ownerToRemoveUsername);
        logger.info("Removed {} as owner from store {}", ownerToRemoveUsername, storeId);
    }
    
    /**
     * Adds a store manager
     * 
     * @param storeId The UUID of the store
     * @param ownerUsername The username of the owner
     * @param managerUsername The username of the new manager
     * @throws IllegalArgumentException if store doesn't exist or users aren't valid
     */
    public void addStoreManager(UUID storeId, String ownerUsername, String managerUsername) {
        logger.info("Adding store manager {} to store {} by {}", 
                    managerUsername, storeId, ownerUsername);
        
        // Check if store exists
        if (!storeRepository.exists(storeId)) {
            logger.error("Store with ID {} not found", storeId);
            throw new IllegalArgumentException("Store not found");
        }
        
        // Check if current user is an owner
        if (!storeRepository.isOwner(storeId, ownerUsername)) {
            logger.error("User {} is not a store owner", ownerUsername);
            throw new IllegalArgumentException("Only store owners can add managers");
        }
        
        // Check if new manager is already a manager
        if (storeRepository.isManager(storeId, managerUsername)) {
            logger.error("User {} is already a store manager", managerUsername);
            throw new IllegalArgumentException("User is already a store manager");
        }
        
        // Check if new manager is already an owner
        if (storeRepository.isOwner(storeId, managerUsername)) {
            logger.error("User {} is already a store owner", managerUsername);
            throw new IllegalArgumentException("User is already a store owner");
        }
        
        // Add the new manager
        storeRepository.addManager(storeId, managerUsername);
        logger.info("Added {} as manager to store {}", managerUsername, storeId);
    }
    
    /**
     * Removes a store manager
     * 
     * @param storeId The UUID of the store
     * @param ownerUsername The username of the owner
     * @param managerUsername The username of the manager to remove
     * @throws IllegalArgumentException if store doesn't exist or users aren't valid
     */
    public void removeStoreManager(UUID storeId, String ownerUsername, String managerUsername) {
        logger.info("Removing store manager {} from store {} by {}", 
                    managerUsername, storeId, ownerUsername);
        
        // Check if store exists
        if (!storeRepository.exists(storeId)) {
            logger.error("Store with ID {} not found", storeId);
            throw new IllegalArgumentException("Store not found");
        }
        
        // Check if current user is an owner
        if (!storeRepository.isOwner(storeId, ownerUsername)) {
            logger.error("User {} is not a store owner", ownerUsername);
            throw new IllegalArgumentException("Only store owners can remove managers");
        }
        
        // Check if manager to remove is a manager
        if (!storeRepository.isManager(storeId, managerUsername)) {
            logger.error("User {} is not a store manager", managerUsername);
            throw new IllegalArgumentException("User is not a store manager");
        }
        
        // Remove the manager
        storeRepository.removeManager(storeId, managerUsername);
        logger.info("Removed {} as manager from store {}", managerUsername, storeId);
    }
    
    /**
     * Gets store personnel information (owners and managers)
     * 
     * @param storeId The UUID of the store
     * @param username The username of the requester
     * @return StorePersonnelDTO with owners and managers information
     * @throws IllegalArgumentException if store doesn't exist or user isn't authorized
     */
    public StorePersonnelDTO getStorePersonnel(UUID storeId, String username) {
        logger.info("Getting store personnel for store ID: {} by user: {}", storeId, username);
        
        // Check if store exists
        if (!storeRepository.exists(storeId)) {
            logger.error("Store with ID {} not found", storeId);
            throw new IllegalArgumentException("Store not found");
        }
        
        // Check if user is authorized (owner or manager)
        boolean isAuthorized = storeRepository.isOwner(storeId, username) || 
                               storeRepository.isManager(storeId, username);
        
        if (!isAuthorized) {
            logger.error("User {} is not authorized to view store personnel", username);
            throw new IllegalArgumentException("Only store owners and managers can view store personnel");
        }
        
        // Get the personnel information
        Set<String> owners = storeRepository.getStoreOwners(storeId);
        Set<String> managers = storeRepository.getStoreManagers(storeId);
        String founder = storeRepository.getStoreFounder(storeId);
        
        // Build and return the DTO
        StorePersonnelDTO personnelDTO = new StorePersonnelDTO(storeId, founder, owners, managers);
        logger.info("Returning personnel info for store ID: {}", storeId);
        return personnelDTO;
    }
    
    /**
     * Updates store information
     * 
     * @param storeId The UUID of the store
     * @param username The username of the requester
     * @param storeRequest The updated store information
     * @throws IllegalArgumentException if store doesn't exist or user isn't authorized
     */
    public void updateStoreInfo(UUID storeId, String username, StoreRequest storeRequest) {
        logger.info("Updating store info for store ID: {} by user: {}", storeId, username);
        
        // Check if store exists
        Optional<Store> storeOpt = storeRepository.findById(storeId);
        if (!storeOpt.isPresent()) {
            logger.error("Store with ID {} not found", storeId);
            throw new IllegalArgumentException("Store not found");
        }
        
        // Check if user is authorized (owner)
        if (!storeRepository.isOwner(storeId, username)) {
            logger.error("User {} is not authorized to update store info", username);
            throw new IllegalArgumentException("Only store owners can update store information");
        }
        
        // Update the store
        Store store = storeOpt.get();
        
        if (storeRequest.getStoreName() != null && !storeRequest.getStoreName().isEmpty()) {
            store.setName(storeRequest.getStoreName());
        }
        
        if (storeRequest.getDescription() != null) {
            store.setDescription(storeRequest.getDescription());
        }
        
        storeRepository.save(store);
        logger.info("Store info updated successfully for store ID: {}", storeId);
    }
    
    /**
     * Receives a message for a store from a user
     * 
     * @param storeId The UUID of the store
     * @param username The username of the sender
     * @param message The message content
     * @throws IllegalArgumentException if store doesn't exist
     */
    public void receiveMessage(UUID storeId, String username, String message) {
        logger.info("Receiving message for store ID: {} from user: {}", storeId, username);
        
        // Check if store exists
        if (!storeRepository.exists(storeId)) {
            logger.error("Store with ID {} not found", storeId);
            throw new IllegalArgumentException("Store not found");
        }
        
        // In a real implementation, we would store the message and notify store owners/managers
        logger.info("Message from user {} to store {}: {}", username, storeId, message);
        
        // Example of how we might implement notifications in future versions:
        // Set<String> owners = storeRepository.getStoreOwners(storeId);
        // Set<String> managers = storeRepository.getStoreManagers(storeId);
        // notificationService.notifyUsers(owners, "New message from " + username);
        // notificationService.notifyUsers(managers, "New message from " + username);
    }
    
    /**
     * Gets the store order history
     * 
     * @param storeId The UUID of the store
     * @param username The username of the requester
     * @return List of OrderDTO objects representing store orders
     * @throws IllegalArgumentException if store doesn't exist or user isn't authorized
     */
    public List<OrderDTO> getStoreOrderHistory(UUID storeId, String username) {
        logger.info("Getting order history for store ID: {} by user: {}", storeId, username);
        
        // Check if store exists
        if (!storeRepository.exists(storeId)) {
            logger.error("Store with ID {} not found", storeId);
            throw new IllegalArgumentException("Store not found");
        }
        
        // Check if user is authorized (owner or manager)
        boolean isAuthorized = storeRepository.isOwner(storeId, username) || 
                              storeRepository.isManager(storeId, username);
        
        if (!isAuthorized) {
            logger.error("User {} is not authorized to view store order history", username);
            throw new IllegalArgumentException("Only store owners and managers can view store order history");
        }
        
        // Get the store's order IDs
        List<UUID> orderIds = storeRepository.getStoreOrdersIds(storeId);
        
        // In a full implementation, we would retrieve the actual orders from an order repository
        // and convert them to DTOs. For this implementation, we'll return an empty list.
        List<OrderDTO> orderDTOs = new ArrayList<>();
        
        logger.info("Returning {} orders for store ID: {}", orderDTOs.size(), storeId);
        return orderDTOs;
    }
    
    /**
     * Searches for stores matching the given criteria
     * 
     * @param searchRequest The search criteria
     * @return List of StoreDTO objects matching the criteria
     */
    public List<StoreDTO> searchStore(SearchRequest searchRequest) {
        logger.info("Searching for stores with criteria: {}", searchRequest);
        
        // In a full implementation, we would use repository methods to search for stores
        // based on name, category, product, etc.
        // For now, we'll just return all active stores
        
        List<Store> stores = storeRepository.findAll();
        List<StoreDTO> result = new ArrayList<>();
        
        for (Store store : stores) {
            if (store.isActive()) {
                // Apply search criteria if provided
                boolean matches = true;
                
                if (searchRequest.getName() != null && !searchRequest.getName().isEmpty()) {
                    matches = store.getName().toLowerCase().contains(searchRequest.getName().toLowerCase());
                }
                
                if (matches && searchRequest.getProductCategory() != null && !searchRequest.getProductCategory().isEmpty()) {
                    // In a real implementation, we would check if the store has products in this category
                    // This is a placeholder
                    Set<Store> storesWithCategory = storeRepository.findByProductCategory(searchRequest.getProductCategory());
                    matches = storesWithCategory.contains(store);
                }
                
                if (matches) {
                    result.add(new StoreDTO(
                        store.getStoreId(),
                        store.getName(),
                        store.getDescription(),
                        true,
                        store.getFounder().getUsername(),
                        storeRepository.getStoreOwners(store.getStoreId()),
                        storeRepository.getStoreManagers(store.getStoreId())
                    ));
                }
            }
        }
        
        logger.info("Found {} stores matching search criteria", result.size());
        return result;
    }
}