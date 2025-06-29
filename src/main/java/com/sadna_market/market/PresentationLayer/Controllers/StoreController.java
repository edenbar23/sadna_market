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
import org.springframework.http.MediaType;
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

    // ───────────── Store CRUD ─────────────

    @GetMapping
    public ResponseEntity<Response<List<StoreDTO>>> getAllStores() {
        logger.info("Getting all stores");
        Response<List<StoreDTO>> response = storeService.getAllStores();

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<Response<StoreDTO>> getStoreById(@PathVariable UUID storeId) {
        logger.info("Getting store with ID: {}", storeId);
        Response<StoreDTO> response = storeService.getStoreInfo(storeId);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

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

    // ───────────── Store Status ─────────────

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

    @GetMapping("/{storeId}/status")
    public ResponseEntity<Response<Boolean>> getStoreStatus(@PathVariable UUID storeId) {
        logger.info("Getting status for store with ID: {}", storeId);
        Response<Boolean> response = storeService.getStoreStatus(storeId);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ───────────── Store Ratings ─────────────

    @GetMapping("/top-rated")
    public ResponseEntity<Response<List<Store>>> getTopRatedStores() {
        logger.info("Getting top rated stores");
        Response<List<Store>> response = storeService.getTopRatedStores();

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }

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

    // ───────────── Store Owners ─────────────

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

    // ───────────── Store Managers ─────────────

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

    // ───────────── NEW: Update Owner/Manager Permissions ─────────────

    /**
     * Update permissions for a specific store owner
     */
    @PutMapping("/{storeId}/owners/{ownerUsername}/permissions")
public ResponseEntity<Response<String>> updateOwnerPermissions(
        @PathVariable UUID storeId,
        @PathVariable String ownerUsername,
        @RequestHeader("Authorization") String token,
        @RequestBody PermissionsRequest request,
        @RequestParam(name = "byUser", required = false) String byUser) {

    logger.info("🔧 Incoming request to update owner permissions:");
    logger.info("   storeId: {}", storeId);
    logger.info("   ownerUsername: {}", ownerUsername);
    logger.info("   byUser: {}", byUser);
    logger.info("   token: {}", token);
    if (request == null) {
        logger.warn("   ❗ PermissionsRequest is NULL");
    } else {
        logger.info("   permissions: {}", request.getPermissions());
    }

    Response<String> response = storeService.changePermissions(byUser, token, storeId, ownerUsername, request);

    if (response.isError()) {
        logger.error("❌ Error: {}", response.getErrorMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    return ResponseEntity.ok(response);
}


    /**
     * Update permissions for a specific store manager
     */
    @PutMapping("/{storeId}/managers/{managerUsername}/permissions")
    public ResponseEntity<Response<String>> updateManagerPermissions(
            @PathVariable UUID storeId,
            @PathVariable String managerUsername,
            @RequestHeader("Authorization") String token,
            @RequestBody PermissionsRequest request,
            @RequestParam(name = "byUser", required = false) String byUser // acting user
    ) {
        logger.info("Updating manager permissions for {} in store {} by {}", managerUsername, storeId, byUser);
        Response<String> response = storeService.changePermissions(byUser, token, storeId, managerUsername, request);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ───────────── Store Messages ─────────────

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

    // ───────────── Store Orders ─────────────

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

    @GetMapping(
    value = "/{storeId}/permissions/{username}",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public ResponseEntity<Set<Permission>> getPermissionsForUser(
        @RequestHeader("Authorization") String authorization,
        @RequestParam("byUser") String requester,
        @PathVariable UUID storeId,
        @PathVariable String username) {

            System.out.println("⭐️ [BACKEND] getPermissionsForUser CALLED for user: " + username);        
    String token = authorization.replace("Bearer ", "");
    Response<Set<Permission>> response = storeService.getPermissionsForUser(requester, token, storeId, username);

    if (response.isError()) {
        // Always return JSON, even for errors
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Set.of());
    }
    // Unwrap the permissions so the frontend gets raw JSON array (["VIEW_STORE_INFO", ...])
    return ResponseEntity.ok(response.getData());
}
    

}
