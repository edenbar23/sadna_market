package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.ApplicationLayer.DTOs.OrderDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.StoreDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.StorePersonnelDTO;
import com.sadna_market.market.ApplicationLayer.Requests.PermissionsRequest;
import com.sadna_market.market.ApplicationLayer.Requests.SearchRequest;
import com.sadna_market.market.ApplicationLayer.Requests.StoreRequest;
import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.DomainServices.StoreManagementService;
import com.sadna_market.market.DomainLayer.StoreExceptions.*;
import com.sadna_market.market.InfrastructureLayer.RepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDateTime;
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
    private final IOrderRepository orderRepository;
    private final ObjectMapper objectMapper;


    private StoreService(RepositoryConfiguration RC) {
        this.storeRepository = RC.storeRepository();
        this.orderRepository = RC.orderRepository();
        this.storeManagementService = StoreManagementService.getInstance(storeRepository, RC.userRepository());
        this.objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Enable Java 8 date/time modules
        logger.info("StoreService initialized");
    }

    /**
     * Get singleton instance with repository dependency resolution
     */
    public static synchronized StoreService getInstance(RepositoryConfiguration RC) {
        if (instance == null) {
            instance = new StoreService(RC);
        }
        return instance;
    }


    /**
     * Opens a new store with the given details
     * Orchestrates the domain service and handles error mapping
     */
    public Response openStore(String username, String token, StoreRequest storeRequest) {
        logger.info("Opening new store with name: {} for founder: {}",
                storeRequest.getStoreName(), username);

        try {
            // Set founder username from authenticated user
            if (storeRequest.getFounderUsername() == null) {
                storeRequest.setFounderUsername(username);
            } else if (!storeRequest.getFounderUsername().equals(username)) {
                return Response.error("Cannot open store for another user");
            }

            Store store = storeManagementService.createStore(
                    username,
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
        } catch (StoreNotActiveException e) {
            logger.error("Store not active: {}", e.getMessage());
            return Response.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error removing owner: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to remove store owner");
        }
    }

    /**
     * Appoints a store manager with specific permissions
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
    public Response changePermissions(String updaterUsername, UUID storeId, String managerUsername,
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
                    new ArrayList<>(store.getOwnerUsernames()),
                    new ArrayList<>(store.getManagerUsernames())
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
     * Enhanced to include product category searches via product repository
     */
    public Response searchStore(SearchRequest searchRequest) {
        logger.info("Searching stores with criteria: {}", searchRequest);

        try {
            List<Store> stores = storeRepository.findAll();
            Set<Store> result = new HashSet<>();

            // Basic name filtering
            if (searchRequest.getName() != null && !searchRequest.getName().isEmpty()) {
                stores.stream()
                        .filter(store -> store.isActive() &&
                                store.getName().toLowerCase().contains(searchRequest.getName().toLowerCase()))
                        .forEach(result::add);
            }

            // Product category filtering - if specified
            if (searchRequest.getProductCategory() != null && !searchRequest.getProductCategory().isEmpty()) {
                Set<Store> storesByCategory = storeRepository.findByProductCategory(searchRequest.getProductCategory());

                if (result.isEmpty()) {
                    // No name filter was applied
                    result.addAll(storesByCategory.stream()
                            .filter(Store::isActive)
                            .collect(Collectors.toSet()));
                } else {
                    // Name filter was applied, intersect the results
                    result.retainAll(storesByCategory);
                }
            }

            // If no filters were applied, return all active stores
            if ((searchRequest.getName() == null || searchRequest.getName().isEmpty()) &&
                    (searchRequest.getProductCategory() == null || searchRequest.getProductCategory().isEmpty())) {
                result = stores.stream()
                        .filter(Store::isActive)
                        .collect(Collectors.toSet());
            }

            List<StoreDTO> storeDTOs = result.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            String json = objectMapper.writeValueAsString(storeDTOs);
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
     * Gets store purchase history - Requires admin or store owner/manager permission
     */
    public Response getStorePurchaseHistory(String username, UUID storeId) {
        logger.info("Getting purchase history for store ID: {} requested by user: {}", storeId, username);

        try {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new StoreNotFoundException("Store not found"));

            // Check if user has permission to view order history
            if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
                throw new InsufficientPermissionsException("Not authorized to view store order history");
            }

            List<Order> orders = orderRepository.getStorePurchaseHistory(storeId);

            // Convert to DTOs
            List<OrderDTO> orderDTOs = orders.stream()
                    .map(OrderDTO::new)
                    .collect(Collectors.toList());

            String json = objectMapper.writeValueAsString(orderDTOs);
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

    /**
     * Gets buyer rate statistics for the system admin
     * Returns data about shopping patterns over time
     */
    public Response getBuyersRate(String adminUsername) {
        logger.info("Getting buyers rate statistics requested by admin: {}", adminUsername);

        try {

            // Statistics map to hold our results
            Map<String, Object> statistics = new HashMap<>();

            // Get all orders
            List<Order> allOrders = orderRepository.findAll();

            // Count orders by date ranges
            LocalDateTime now = LocalDateTime.now();

            // Last day
            long lastDayOrders = countOrdersInRange(allOrders, now.minusDays(1), now);
            statistics.put("ordersLast24Hours", lastDayOrders);

            // Last week
            long lastWeekOrders = countOrdersInRange(allOrders, now.minusDays(7), now);
            statistics.put("ordersLastWeek", lastWeekOrders);

            // Last month
            long lastMonthOrders = countOrdersInRange(allOrders, now.minusDays(30), now);
            statistics.put("ordersLastMonth", lastMonthOrders);

            // Get unique buyers count
            long uniqueBuyers = allOrders.stream()
                    .map(Order::getUserName)
                    .distinct()
                    .count();
            statistics.put("uniqueBuyers", uniqueBuyers);

            // Average orders per store
            Map<UUID, Long> ordersByStore = allOrders.stream()
                    .collect(Collectors.groupingBy(Order::getStoreId, Collectors.counting()));

            double avgOrdersPerStore = !ordersByStore.isEmpty() ?
                    (double) allOrders.size() / ordersByStore.size() : 0;
            statistics.put("averageOrdersPerStore", avgOrdersPerStore);

            // Average order value
            double avgOrderValue = allOrders.stream()
                    .mapToDouble(Order::getFinalPrice)
                    .average()
                    .orElse(0);
            statistics.put("averageOrderValue", avgOrderValue);

            String json = objectMapper.writeValueAsString(statistics);
            return Response.success(json);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize buyers rate statistics: {}", e.getMessage());
            return Response.error("Internal error: Failed to process statistics");
        } catch (Exception e) {
            logger.error("Unexpected error getting buyers rate statistics: {}", e.getMessage(), e);
            return Response.error("Internal error: Failed to get buyers rate statistics");
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

    private long countOrdersInRange(List<Order> orders, LocalDateTime startDate, LocalDateTime endDate) {
        return orders.stream()
                .filter(order -> {
                    LocalDateTime orderDate = order.getOrderDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .count();
    }

    /**
     * Reset the singleton instance (primarily for testing)
     */
    public static synchronized void reset() {
        instance = null;
        logger.info("StoreService instance reset");
    }
}