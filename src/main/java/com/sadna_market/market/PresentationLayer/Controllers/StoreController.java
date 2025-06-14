package com.sadna_market.market.PresentationLayer.Controllers;

import com.sadna_market.market.ApplicationLayer.DTOs.*;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.ApplicationLayer.Response;
import com.sadna_market.market.ApplicationLayer.MessageApplicationService;
import com.sadna_market.market.DomainLayer.DomainServices.StoreManagementService;
import com.sadna_market.market.DomainLayer.Message;
import com.sadna_market.market.DomainLayer.Permission;
import com.sadna_market.market.DomainLayer.Store;
import com.sadna_market.market.ApplicationLayer.StoreService;
import com.sadna_market.market.ApplicationLayer.ProductService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Set;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*") // Update for production with specific origins
public class StoreController {

    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);
    private final StoreService storeService;
    private final ProductService productService;
    private final MessageApplicationService messageService;

    @Autowired
    public StoreController(StoreService storeService, ProductService productService,
                           MessageApplicationService messageService) {
        this.storeService = storeService;
        this.productService = productService;
        this.messageService = messageService;
    }

    /**
     * Get all stores
     */
    @GetMapping
    public ResponseEntity<Response<List<StoreDTO>>> getAllStores() {
        logger.info("Getting all stores");
        Response<List<StoreDTO>> response = storeService.getAllStores();

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get store information by ID
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<Response<StoreDTO>> getStoreById(@PathVariable UUID storeId) {
        logger.info("Getting store with ID: {}", storeId);
        Response<StoreDTO> response = storeService.getStoreInfo(storeId);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Create a new store
     */
    @PostMapping
    public ResponseEntity<Response<StoreDTO>> createStore(
            @RequestBody StoreRequest storeRequest,
            @RequestHeader("Authorization") String token) {

        String username = storeRequest.getFounderUsername();
        logger.info("Creating store with name: {} for user: {}", storeRequest.getStoreName(), username);
        Response<StoreDTO> response = storeService.createStore(storeRequest.getFounderUsername(), token, storeRequest);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Close a store
     */
    @PostMapping("/{storeId}/close")
    public ResponseEntity<Response<String>> closeStore(
            @PathVariable UUID storeId,
            @RequestHeader("Authorization") String token,
            @RequestParam String founderUserName) {

        logger.info("Closing store with ID: {} by user: {}", storeId, founderUserName);
        Response<String> response = storeService.closeStore(founderUserName, token, storeId);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Reopen a store
     */
    @PostMapping("/{storeId}/reopen")
    public ResponseEntity<Response<String>> reopenStore(
            @PathVariable UUID storeId,
            @RequestHeader("Authorization") String token,
            @RequestParam String founderUserName) {

        logger.info("Reopening store with ID: {} by user: {}", storeId, founderUserName);
        Response<String> response = storeService.reopenStore(founderUserName, token, storeId);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get store status (active/inactive)
     */
    @GetMapping("/{storeId}/status")
    public ResponseEntity<Response<Boolean>> getStoreStatus(@PathVariable UUID storeId) {
        logger.info("Getting status for store with ID: {}", storeId);
        Response<Boolean> response = storeService.getStoreStatus(storeId);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get top rated stores
     */
    @GetMapping("/top-rated")
    public ResponseEntity<Response<List<Store>>> getTopRatedStores() {
        logger.info("Getting top rated stores");
        Response<List<Store>> response = storeService.getTopRatedStores();

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Appoint a new store owner
     */
    @PostMapping("/{storeId}/owners")
    public ResponseEntity<Response<String>> appointOwner(
            @PathVariable UUID storeId,
            @RequestBody AppointOwnerRequest request,
            @RequestHeader("Authorization") String token) {

        logger.info("Appointing owner: {} appoints {} in store {}",
                request.getFounderUserName(), request.getNewOwnerUserName(), storeId);

        Response<String> response = storeService.appointStoreOwner(
                request.getFounderUserName(), token, storeId, request.getNewOwnerUserName());

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Remove a store owner
     */
    @DeleteMapping("/{storeId}/owners")
    public ResponseEntity<Response<String>> removeOwner(
            @PathVariable UUID storeId,
            @RequestBody RemoveOwnerRequest request,
            @RequestHeader("Authorization") String token) {

        logger.info("Removing owner: {} removes {} in store {}",
                request.getFounderUserName(), request.getRemovedOwnerUserName(), storeId);

        Response<String> response = storeService.removeStoreOwner(
                request.getFounderUserName(), token, storeId, request.getRemovedOwnerUserName());

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Appoint a new store manager
     */
    @PostMapping("/{storeId}/managers")
    public ResponseEntity<Response<String>> appointManager(
            @PathVariable UUID storeId,
            @RequestBody AppointManagerRequest request,
            @RequestHeader("Authorization") String token) {

        logger.info("Appointing manager: {} appoints {} in store {}",
                request.getAppointingUserName(), request.getNewManagerUserName(), storeId);

        Response<String> response = storeService.appointStoreManager(
                request.getAppointingUserName(), token, storeId,
                request.getNewManagerUserName(), new PermissionsRequest(request.getPermissions()));

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Remove a store manager
     */
    @DeleteMapping("/{storeId}/managers")
    public ResponseEntity<Response<String>> removeManager(
            @PathVariable UUID storeId,
            @RequestBody RemoveManagerRequest request,
            @RequestHeader("Authorization") String token) {

        logger.info("Removing manager: {} removes {} in store {}",
                request.getAppointingUserName(), request.getRemovedManagerUserName(), storeId);

        Response<String> response = storeService.removeStoreManager(
                request.getAppointingUserName(), token, storeId, request.getRemovedManagerUserName());

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get store messages
     */
    @GetMapping("/{storeId}/messages")
    public ResponseEntity<Response<List<MessageDTO>>> getStoreMessages(
            @PathVariable UUID storeId,
            @RequestHeader("Authorization") String token,
            @RequestParam String username) {

        logger.info("Getting messages for store: {} by user: {}", storeId, username);
        Response<List<MessageDTO>> response = messageService.getStoreMessages(username, token, storeId);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get store orders
     */
    @GetMapping("/{storeId}/orders")
    public ResponseEntity<Response<List<OrderDTO>>> getStoreOrders(
            @PathVariable UUID storeId,
            @RequestHeader("Authorization") String token,
            @RequestParam String username) {

        logger.info("Getting orders for store: {} by user: {}", storeId, username);

        Response<List<OrderDTO>> response = storeService.getStoreOrders(username, token, storeId);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    // Helper method to get store purchase history (since storeService doesn't have this method directly)
    private Response<List<OrderDTO>> getStorePurchaseHistory(String username, String token, UUID storeId) {
        // This would typically be implemented in StoreService
        // For now, return an error that directs to implement this method
        return Response.error("Store purchase history method needs to be implemented in StoreService");
    }

    /**
     * Rate a store
     */
    @PostMapping("/rate")
    public ResponseEntity<Response<StoreRatingDTO>> rateStore(
            @RequestBody StoreRateRequest request,
            @RequestHeader("Authorization") String token) {

        logger.info("Rating store: {} by user: {} with rating: {}",
                request.getStoreId(), request.getUsername(), request.getRate());

        Response<StoreRatingDTO> response = storeService.rateStore(token, request);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Rename a store
     */
    @PutMapping("/{storeId}/rename")
    public ResponseEntity<Response<StoreDTO>> renameStore(
            @PathVariable UUID storeId,
            @RequestHeader("Authorization") String token,
            @RequestParam String username,
            @RequestParam String newName) {

        logger.info("Renaming store {} to {} by user {}", storeId, newName, username);
        Response<StoreDTO> response = storeService.renameStore(username, token, storeId, newName);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }
}