package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.ApplicationLayer.DTOs.StoreDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.StorePersonnelDTO;
import com.sadna_market.market.ApplicationLayer.Requests.PermissionsRequest;
import com.sadna_market.market.ApplicationLayer.Requests.SearchRequest;
import com.sadna_market.market.ApplicationLayer.Requests.StoreRequest;
import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.DomainServices.StoreManagementService;
import com.sadna_market.market.DomainLayer.StoreExceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Application layer service for store operations.
 * Orchestrates between domain services and repositories, handles DTOs and error mapping.
 */
@Service
public class StoreService {

    private static StoreService instance;

    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    private final StoreManagementService storeManagementService;
    private final IStoreRepository storeRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    private StoreService(IStoreRepository storeRepository, IUserRepository userRepository) {
        this.storeRepository = storeRepository.getInstance();
        this.storeManagementService = new StoreManagementService(storeRepository, userRepository);
        this.objectMapper = new ObjectMapper();
        logger.info("StoreService initialized");
    }

    public static synchronized StoreService getInstance() {
        if (instance == null) {
            instance = new StoreService(IStoreRepository storeRepository, IUserRepository userRepository);
        }
        return instance;
    }

    /**
     * Opens a new store with the given details
     * Orchestrates the domain service and handles error mapping
     */
    public Response openStore(StoreRequest storeRequest) {
        String founderUsername = storeRequest.getFounderUsername();
        logger.info("Opening new store with name: {} for founder: {}",
                storeRequest.getStoreName(), founderUsername);

        try {
            Store store = storeManagementService.createStore(
                    founderUsername,
                    storeRequest.getStoreName(),
                    storeRequest.getDescription(),
                    storeRequest.getAddress(),
                    storeRequest.getEmail(),
                    storeRequest.getPhoneNumber()
            );

            StoreDTO storeDTO = convertToDTO(store);
            String json = objectMapper.writeValueAsString(storeDTO);
            return Response.success(json);

        } catch (StoreExceptions.UserNotFoundException e) {
            logger.error("Store creation failed - user not found: {}", e.getMessage());
            return Response.error("User not found: " + e.getMessage());
        } catch (StoreAlreadyExistsException e) {
            logger.error("Store creation failed - store already exists: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (InvalidStoreDataException e) {
            logger.error("Store creation failed - invalid data: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize store response: {}", e.getMessage());
            return Response.error("Internal error: Failed to process response");
        } catch (Exception e) {
            logger.error("Unexpected error creating store: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to create store");
        }
    }

    /**
     * Closes a store
     * Delegates to domain service for business rule enforcement
     */
    public Response closeStore(String username, UUID storeId) {
        logger.info("Attempting to close store with ID: {} by user: {}", storeId, username);

        try {
            storeManagementService.closeStore(username, storeId);
            return Response.success("Store closed successfully");

        } catch (StoreNotFoundException e) {
            logger.error("Store not found: {}", storeId);
            return Response.error("Store not found");
        } catch (InsufficientPermissionsException e) {
            logger.error("Permission denied for user {}: {}", username, e.getMessage());
            return Response.error(e.getMessage());
        } catch (StoreAlreadyClosedException e) {
            logger.error("Store already closed: {}", storeId);
            return Response.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error closing store: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to close store");
        }
    }

    /**
     * Reopens a closed store
     */
    public Response reopenStore(String username, UUID storeId) {
        logger.info("Attempting to reopen store with ID: {} by user: {}", storeId, username);

        try {
            storeManagementService.reopenStore(username, storeId);
            return Response.success("Store reopened successfully");

        } catch (StoreNotFoundException e) {
            logger.error("Store not found: {}", storeId);
            return Response.error("Store not found");
        } catch (InsufficientPermissionsException e) {
            logger.error("Permission denied for user {}: {}", username, e.getMessage());
            return Response.error(e.getMessage());
        } catch (StoreAlreadyOpenException e) {
            logger.error("Store already open: {}", storeId);
            return Response.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error reopening store: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to reopen store");
        }
    }

    /**
     * Gets information about a store - Simple repository operation
     */
    public Response getStoreInfo(UUID storeId) {
        logger.info("Getting store info for store ID: {}", storeId);

        try {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

            StoreDTO storeDTO = convertToDTO(store);
            String json = objectMapper.writeValueAsString(storeDTO);
            return Response.success(json);

        } catch (StoreNotFoundException e) {
            logger.error("Store not found: {}", storeId);
            return Response.error("Store not found");
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize store response: {}", e.getMessage());
            return Response.error("Internal error: Failed to process response");
        } catch (Exception e) {
            logger.error("Unexpected error getting store info: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to get store information");
        }
    }

    /**
     * Gets all stores in the system - Simple repository operation
     */
    public Response getAllStores() {
        logger.info("Getting all stores");

        try {
            List<Store> stores = storeRepository.findAll();
            List<StoreDTO> storeDTOs = stores.stream()
                    .filter(Store::isActive)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            String json = objectMapper.writeValueAsString(storeDTOs);
            return Response.success(json);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize stores response: {}", e.getMessage());
            return Response.error("Internal error: Failed to process response");
        } catch (Exception e) {
            logger.error("Unexpected error getting all stores: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to get stores");
        }
    }

    /**
     * Appoints a store owner
     */
    public Response appointStoreOwner(String appointerUsername, UUID storeId, String newOwnerUsername) {
        logger.info("User {} appointing {} as store owner for store {}",
                appointerUsername, newOwnerUsername, storeId);

        try {
            storeManagementService.appointStoreOwner(appointerUsername, storeId, newOwnerUsername);
            return Response.success("Store owner appointed successfully");

        } catch (StoreNotFoundException e) {
            logger.error("Store not found: {}", storeId);
            return Response.error("Store not found");
        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (InsufficientPermissionsException e) {
            logger.error("Permission denied: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (StoreNotActiveException e) {
            logger.error("Store not active: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error appointing owner: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to appoint store owner");
        }
    }

    /**
     * Removes a store owner
     */
    public Response removeStoreOwner(String removerUsername, UUID storeId, String ownerToRemove) {
        logger.info("User {} removing {} as store owner from store {}",
                removerUsername, ownerToRemove, storeId);

        try {
            storeManagementService.removeStoreOwner(removerUsername, storeId, ownerToRemove);
            return Response.success("Store owner removed successfully");

        } catch (StoreNotFoundException e) {
            logger.error("Store not found: {}", storeId);
            return Response.error("Store not found");
        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (InsufficientPermissionsException e) {
            logger.error("Permission denied: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (CannotRemoveFounderException e) {
            logger.error("Cannot remove founder: {}", e.getMessage());
            return Response.error(e.getMessage());
        }  catch (StoreNotActiveException e) {
            logger.error("Store not active: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error removing owner: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to remove store owner");
        }
    }

    /**
     * Appoints a store manager
     */
    public Response appointStoreManager(String appointerUsername, UUID storeId, String newManagerUsername,
                                        PermissionsRequest permissionsRequest) {
        logger.info("User {} appointing {} as store manager for store {}",
                appointerUsername, newManagerUsername, storeId);

        try {
            Set<Permission> permissions = permissionsRequest != null ?
                    permissionsRequest.getPermissions() : new HashSet<>();

            storeManagementService.appointStoreManager(appointerUsername, storeId,
                    newManagerUsername, permissions);
            return Response.success("Store manager appointed successfully");

        } catch (StoreNotFoundException e) {
            logger.error("Store not found: {}", storeId);
            return Response.error("Store not found");
        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (InsufficientPermissionsException e) {
            logger.error("Permission denied: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (StoreNotActiveException e) {
            logger.error("Store not active: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error appointing manager: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to appoint store manager");
        }
    }

    /**
     * Removes a store manager
     */
    public Response removeStoreManager(String removerUsername, UUID storeId, String managerToRemove) {
        logger.info("User {} removing {} as store manager from store {}",
                removerUsername, managerToRemove, storeId);

        try {
            storeManagementService.removeStoreManager(removerUsername, storeId, managerToRemove);
            return Response.success("Store manager removed successfully");

        } catch (StoreNotFoundException e) {
            logger.error("Store not found: {}", storeId);
            return Response.error("Store not found");
        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (InsufficientPermissionsException e) {
            logger.error("Permission denied: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (UserNotManagerException e) {
            logger.error("User not manager: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (StoreNotActiveException e) {
            logger.error("Store not active: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error removing manager: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to remove store manager");
        }
    }

    /**
     * Updates manager permissions
     */
    public Response updateManagerPermissions(String updaterUsername, UUID storeId, String managerUsername,
                                             PermissionsRequest permissionsRequest) {
        logger.info("User {} updating permissions for manager {} in store {}",
                updaterUsername, managerUsername, storeId);

        try {
            Set<Permission> permissions = permissionsRequest != null ?
                    permissionsRequest.getPermissions() : new HashSet<>();

            storeManagementService.updateManagerPermissions(updaterUsername, storeId,
                    managerUsername, permissions);
            return Response.success("Manager permissions updated successfully");

        } catch (StoreNotFoundException e) {
            logger.error("Store not found: {}", storeId);
            return Response.error("Store not found");
        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (InsufficientPermissionsException e) {
            logger.error("Permission denied: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (UserNotManagerException e) {
            logger.error("User not manager: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (StoreNotActiveException e) {
            logger.error("Store not active: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating permissions: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to update manager permissions");
        }
    }

    /**
     * Gets store personnel information (owners and managers)
     * Repository operation with permission check
     */
    public Response getStorePersonnel(String username, UUID storeId) {
        logger.info("Getting store personnel for store ID: {} by user: {}", storeId, username);

        try {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new StoreNotFoundException("Store not found"));

            // Check if user has permission to view personnel
            if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
                throw new InsufficientPermissionsException("Not authorized to view store personnel");
            }

            StorePersonnelDTO personnelDTO = new StorePersonnelDTO(
                    storeId,
                    store.getFounder().getUsername(),
                    store.getOwnerUsernames(),
                    store.getManagerUsernames()
            );

            String json = objectMapper.writeValueAsString(personnelDTO);
            return Response.success(json);

        } catch (StoreNotFoundException e) {
            logger.error("Store not found: {}", storeId);
            return Response.error("Store not found");
        } catch (InsufficientPermissionsException e) {
            logger.error("Permission denied: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize personnel response: {}", e.getMessage());
            return Response.error("Internal error: Failed to process response");
        } catch (Exception e) {
            logger.error("Unexpected error getting store personnel: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to get store personnel");
        }
    }

    /**
     * Searches for stores matching criteria
     * Simple repository operation with filtering
     */
    public Response searchStore(SearchRequest searchRequest) {
        logger.info("Searching stores with criteria: {}", searchRequest);

        try {
            List<Store> stores = storeRepository.findAll();
            List<StoreDTO> result = new ArrayList<>();

            for (Store store : stores) {
                if (store.isActive() && matchesSearchCriteria(store, searchRequest)) {
                    result.add(convertToDTO(store));
                }
            }

            String json = objectMapper.writeValueAsString(result);
            return Response.success(json);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize search response: {}", e.getMessage());
            return Response.error("Internal error: Failed to process response");
        } catch (Exception e) {
            logger.error("Unexpected error searching stores: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to search stores");
        }
    }

    /**
     * Gets store order history
     */
    public Response getStoreOrderHistory(String username, UUID storeId) {
        logger.info("Getting order history for store ID: {} by user: {}", storeId, username);

        try {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new StoreNotFoundException("Store not found"));

            // Check if user has permission to view order history
            if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
                throw new InsufficientPermissionsException("Not authorized to view store order history");
            }

            List<UUID> orderIds = storeRepository.getStoreOrdersIds(storeId);

            // In a full implementation, we would fetch actual orders from order repository
            // For now, return the order IDs
            String json = objectMapper.writeValueAsString(orderIds);
            return Response.success(json);

        } catch (StoreNotFoundException e) {
            logger.error("Store not found: {}", storeId);
            return Response.error("Store not found");
        } catch (InsufficientPermissionsException e) {
            logger.error("Permission denied: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize order history response: {}", e.getMessage());
            return Response.error("Internal error: Failed to process response");
        } catch (Exception e) {
            logger.error("Unexpected error getting order history: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to get order history");
        }
    }

    // Helper methods

    private StoreDTO convertToDTO(Store store) {
        return new StoreDTO(
                store.getStoreId(),
                store.getName(),
                store.getDescription(),
                store.isActive(),
                store.getFounder().getUsername(),
                store.getOwnerUsernames(),
                store.getManagerUsernames()
        );
    }

    private boolean matchesSearchCriteria(Store store, SearchRequest request) {
        if (request.getName() != null && !request.getName().isEmpty()) {
            if (!store.getName().toLowerCase().contains(request.getName().toLowerCase())) {
                return false;
            }
        }

        // Add more search criteria as needed
        // For example, searching by product category would require integration with product service

        return true;
    }

    public static synchronized void reset(){
        instance = null;
        logger.info("StoreService instance reset");
    }

    public Response getBuyersRate(String admin) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBuyersRate'");
    }

    public Response getStorePurchaseHistory(String admin, UUID storeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStorePurchaseHistory'");
    }
//appointStoreManager(username,storeId,manager,permissions);
public Response appointStoreManager(String username,String token,UUID storeId, String manager, PermissionsRequest permissions) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getStorePurchaseHistory'");
}