package com.sadna_market.market.PresentationLayer.Controllers;

import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.ApplicationLayer.Response;
import com.sadna_market.market.DomainLayer.DomainServices.StoreManagementService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*") // Update for production
public class StoreController {

    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);
    private final StoreManagementService storeService;

    @Autowired
    public StoreController(StoreManagementService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
public ResponseEntity<Response<UUID>> createStore(@RequestBody StoreRequest storeRequest) {
    try {
        UUID storeId = storeService.createStore(
                storeRequest.getFounderUsername(),
                storeRequest.getStoreName(),
                storeRequest.getDescription(),
                storeRequest.getAddress(),
                storeRequest.getAddress(),
                storeRequest.getPhoneNumber()
        ).getStoreId();
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.success(storeId));
    } catch (Exception e) {
        logger.error("Error creating store", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.error("Failed to create store: " + e.getMessage()));
    }
}


    @PostMapping("/{storeId}/close")
    public ResponseEntity<Response<String>> closeStore(
            @PathVariable UUID storeId,
            @RequestParam String founderUserName) {
        try {
            storeService.closeStore(founderUserName, storeId);
            return ResponseEntity.ok(Response.success("Store closed successfully"));
        } catch (Exception e) {
            logger.error("Error closing store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to close store: " + e.getMessage()));
        }
    }


    @PostMapping("/{storeId}/reopen")
    public ResponseEntity<Response<String>> reopenStore(
            @PathVariable UUID storeId,
            @RequestParam String founderUserName) {
        try {
            storeService.reopenStore(founderUserName, storeId);
            return ResponseEntity.ok(Response.success("Store reopened successfully"));
        } catch (Exception e) {
            logger.error("Error reopening store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to reopen store: " + e.getMessage()));
        }
    }


    @PostMapping("/{storeId}/owners")
    public ResponseEntity<?> appointOwner(@PathVariable UUID storeId, @RequestBody AppointOwnerRequest request) {
        logger.info("Appointing owner: {} appoints {} in store {}", request.getFounderUserName(), request.getNewOwnerUserName(), storeId);
        try {
            storeService.appointStoreOwner(request.getFounderUserName(), storeId, request.getNewOwnerUserName());
            return ResponseEntity.ok(Response.success("Owner appointed successfully"));
        } catch (Exception e) {
            logger.error("Error appointing owner", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to appoint owner: " + e.getMessage()));
        }
        
    }

    @DeleteMapping("/{storeId}/owners")
    public ResponseEntity<?> removeOwner(@PathVariable UUID storeId, @RequestBody RemoveOwnerRequest request) {
        logger.info("Removing owner: {} removes {} in store {}", request.getFounderUserName(), request.getRemovedOwnerUserName(), storeId);
        try {
            storeService.removeStoreOwner(request.getFounderUserName(), storeId, request.getRemovedOwnerUserName());
            return ResponseEntity.ok(Response.success("Owner removed successfully"));
        } catch (Exception e) {
            logger.error("Error removing owner", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to remove owner: " + e.getMessage()));
        }
    }


    @PostMapping("/{storeId}/managers")
    public ResponseEntity<?> appointManager(@PathVariable UUID storeId, @RequestBody AppointManagerRequest request) {
        logger.info("Appointing manager: {} appoints {} in store {}", request.getAppointingUserName(), request.getNewManagerUserName(), storeId);
        try {
            storeService.appointStoreManager(request.getAppointingUserName(), storeId, request.getNewManagerUserName(), request.getPermissions());
            return ResponseEntity.ok(Response.success("Manager appointed successfully"));
        } catch (Exception e) {
            logger.error("Error appointing manager", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to appoint manager: " + e.getMessage()));
        }
    }


    @DeleteMapping("/{storeId}/managers")
    public ResponseEntity<?> removeManager(@PathVariable UUID storeId, @RequestBody RemoveManagerRequest request) {
        logger.info("Removing manager: {} removes {} in store {}", request.getAppointingUserName(), request.getRemovedManagerUserName(), storeId);
        try {
            storeService.removeStoreManager(request.getAppointingUserName(), storeId, request.getRemovedManagerUserName());
            return ResponseEntity.ok(Response.success("Manager removed successfully"));
        } catch (Exception e) {
            logger.error("Error removing manager", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Failed to remove manager: " + e.getMessage()));
        }
    }


    @GetMapping("/{storeId}/status")
public ResponseEntity<Response<Boolean>> getStoreStatus(@PathVariable UUID storeId) {
    try {
        boolean isActive = storeService.getStoreStatus(storeId);
        return ResponseEntity.ok(Response.success(isActive));
    } catch (Exception e) {
        logger.error("Error getting store status", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.error("Failed to get store status: " + e.getMessage()));
    }
}

}
