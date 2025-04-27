package com.sadna_market.market.ApplicationLayer;

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
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    private final StoreManagementService storeManagementService;
    private final IStoreRepository storeRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public StoreService(IStoreRepository storeRepository, IUserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.storeManagementService = new StoreManagementService(storeRepository, userRepository);
        this.objectMapper = new ObjectMapper();
        logger.info("StoreService initialized");
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
}