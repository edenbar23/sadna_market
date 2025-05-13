package com.sadna_market.market.ApplicationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.DTOs.*;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.DomainServices.RatingService;
import com.sadna_market.market.DomainLayer.DomainServices.StoreManagementService;
import com.sadna_market.market.DomainLayer.StoreExceptions.*;
import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationBridge;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    private final AuthenticationBridge authentication;
    private final StoreManagementService storeManagementService;
    private final IStoreRepository storeRepository;
    private final IOrderRepository orderRepository;
    private final RatingService ratingService;
    private final ObjectMapper objectMapper;

    //req 3.2
    public Response openStore(String username, String token, StoreRequest storeRequest) {
        logger.info("Opening new store with name: {} for founder: {}", storeRequest.getStoreName(), username);

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username,token);

            // Set founder username from authenticated user
            if (storeRequest.getFounderUsername() == null) {
                storeRequest.setFounderUsername(username);
            } else if (!storeRequest.getFounderUsername().equals(username)) {
                return Response.error("Cannot open store for another user");
            }

            // Convert application request to domain parameters
            Store store = storeManagementService.createStore(
                    username,
                    storeRequest.getStoreName(),
                    storeRequest.getDescription(),
                    storeRequest.getAddress(),
                    storeRequest.getEmail(),
                    storeRequest.getPhoneNumber()
            );

            // Convert domain object to DTO for response
            StoreDTO storeDTO = convertToDTO(store);
            String json = objectMapper.writeValueAsString(storeDTO);

            logger.info("Store created successfully: {}", json);
            return Response.success(json);

        } catch (Exception e) {
            logger.error("Error creating store: {}", e.getMessage(), e);
            return Response.error("Failed to create store: " + e.getMessage());
        }
    }

    public Response closeStore(String username, String token, UUID storeId) {
        logger.info("Attempting to close store with ID: {} by user: {}", storeId, username);

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username,token);

            storeManagementService.closeStore(username, storeId);
            return Response.success("Store closed successfully");

        } catch (Exception e) {
            logger.error("Error closing store: {}", e.getMessage(), e);
            return Response.error("Failed to close store: " + e.getMessage());
        }
    }

    public Response reopenStore(String username, String token, UUID storeId) {
        logger.info("Attempting to reopen store with ID: {} by user: {}", storeId, username);

        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username,token);

            storeManagementService.reopenStore(username, storeId);
            return Response.success("Store reopened successfully");

        } catch (Exception e) {
            logger.error("Error reopening store: {}", e.getMessage(), e);
            return Response.error("Failed to reopen store: " + e.getMessage());
        }
    }

    //req 2.1 (b)
    public Response getStoreInfo(UUID storeId) {
        logger.info("Getting store info for store ID: {}", storeId);

        try {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new StoreNotFoundException("Store not found: " + storeId));

            StoreDTO storeDTO = convertToDTO(store);
            String json = objectMapper.writeValueAsString(storeDTO);
            return Response.success(json);

        } catch (Exception e) {
            logger.error("Error getting store info: {}", e.getMessage(), e);
            return Response.error("Failed to get store information: " + e.getMessage());
        }
    }

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

        } catch (Exception e) {
            logger.error("Error getting all stores: {}", e.getMessage(), e);
            return Response.error("Failed to get stores: " + e.getMessage());
        }
    }

    public Response appointStoreOwner(String appointerUsername, String token, UUID storeId, String newOwnerUsername) {
        logger.info("User {} appointing {} as store owner for store {}", appointerUsername, newOwnerUsername, storeId);

        try {
            logger.info("Validating token for user with username: {}", appointerUsername);
            authentication.validateToken(appointerUsername,token);

            storeManagementService.appointStoreOwner(appointerUsername, storeId, newOwnerUsername);
            return Response.success("Store owner appointed successfully");

        } catch (Exception e) {
            logger.error("Error appointing store owner: {}", e.getMessage(), e);
            return Response.error("Failed to appoint store owner: " + e.getMessage());
        }
    }

    public Response removeStoreOwner(String removerUsername, String token, UUID storeId, String ownerToRemove) {
        logger.info("User {} removing {} as store owner from store {}", removerUsername, ownerToRemove, storeId);

        try {
            logger.info("Validating token for user with username: {}", removerUsername);
            authentication.validateToken(removerUsername,token);

            storeManagementService.removeStoreOwner(removerUsername, storeId, ownerToRemove);
            return Response.success("Store owner removed successfully");

        } catch (Exception e) {
            logger.error("Error removing store owner: {}", e.getMessage(), e);
            return Response.error("Failed to remove store owner: " + e.getMessage());
        }
    }

    public Response appointStoreManager(String appointerUsername, String token, UUID storeId, String newManagerUsername,
                                        PermissionsRequest permissionsRequest) {
        logger.info("User {} appointing {} as store manager for store {}", appointerUsername, newManagerUsername, storeId);

        try {
            logger.info("Validating token for user with username: {}", appointerUsername);
            authentication.validateToken(appointerUsername,token);

            Set<Permission> permissions = permissionsRequest != null ?
                    permissionsRequest.getPermissions() : new HashSet<>();

            storeManagementService.appointStoreManager(appointerUsername, storeId, newManagerUsername, permissions);
            return Response.success("Store manager appointed successfully");

        } catch (Exception e) {
            logger.error("Error appointing store manager: {}", e.getMessage(), e);
            return Response.error("Failed to appoint store manager: " + e.getMessage());
        }
    }

    public Response removeStoreManager(String removerUsername, String token, UUID storeId, String managerToRemove) {
        logger.info("User {} removing {} as store manager from store {}", removerUsername, managerToRemove, storeId);

        try {
            logger.info("Validating token for user with username: {}", removerUsername);
            authentication.validateToken(removerUsername,token);

            storeManagementService.removeStoreManager(removerUsername, storeId, managerToRemove);
            return Response.success("Store manager removed successfully");

        } catch (Exception e) {
            logger.error("Error removing store manager: {}", e.getMessage(), e);
            return Response.error("Failed to remove store manager: " + e.getMessage());
        }
    }

    public Response changePermissions(String updaterUsername, String token, UUID storeId, String managerUsername,
                                      PermissionsRequest permissionsRequest) {
        logger.info("User {} updating permissions for manager {} in store {}", updaterUsername, managerUsername, storeId);

        try {
            logger.info("Validating token for user with username: {}", updaterUsername);
            authentication.validateToken(updaterUsername,token);

            Set<Permission> permissions = permissionsRequest != null ?
                    permissionsRequest.getPermissions() : new HashSet<>();

            storeManagementService.updateManagerPermissions(updaterUsername, storeId, managerUsername, permissions);
            return Response.success("Manager permissions updated successfully");

        } catch (Exception e) {
            logger.error("Error updating permissions: {}", e.getMessage(), e);
            return Response.error("Failed to update manager permissions: " + e.getMessage());
        }
    }

    public Response leaveOwnership(String username, String token, UUID storeId) {
        logger.info("User {} leaving store ownership for store {}", username, storeId);
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username,token);

            storeManagementService.leaveOwnership(username, storeId);
            return Response.success("Left ownership successfully");
        } catch (Exception e) {
            logger.error("Error leaving ownership: {}", e.getMessage(), e);
            return Response.error("Failed to leave ownership: " + e.getMessage());
        }
    }

    public Response rateStore(String token, StoreRateRequest rate) {
        try {
            logger.info("Validating token for user with username: {}", rate.getUsername());
            authentication.validateToken(rate.getUsername(), token);

            logger.info("User {} rating store {} with value {}",
                    rate.getUsername(), rate.getStoreId(), rate.getRate());

            // Convert application request to domain parameters
            StoreRating storeRating = ratingService.rateStore(
                    rate.getUsername(),
                    rate.getStoreId(),
                    rate.getRate(),
                    rate.getComment());

            // Convert domain object to DTO for response
            StoreRatingDTO ratingDTO = new StoreRatingDTO(storeRating);
            String json = objectMapper.writeValueAsString(ratingDTO);

            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error rating store: {}", e.getMessage(), e);
            return Response.error("Error rating store: " + e.getMessage());
        }
    }

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

    private List<MessageDTO> convertListMessageToDTO(List<Message> messages) {
        List<MessageDTO> messageDTOs = new ArrayList<>();
        for (Message message : messages) {
            MessageDTO messageDTO = new MessageDTO(message);
            messageDTOs.add(messageDTO);
        }
        return messageDTOs;
    }

    public void clear() {
        storeRepository.clear();
    }
}