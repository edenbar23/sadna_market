package com.sadna_market.market.PresentationLayer.Controllers;

import com.sadna_market.market.ApplicationLayer.ProductService;
import com.sadna_market.market.ApplicationLayer.Requests.ProductRateRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ProductRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ProductSearchRequest;
import com.sadna_market.market.ApplicationLayer.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for product-related operations.
 * Provides endpoints for searching, viewing, and managing products.
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*") // Configure appropriately for production
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Search for products based on various criteria
     */
    @PostMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestBody ProductSearchRequest request) {
        logger.info("Received request to search products");
        Response response = productService.searchProduct(request);

        if (response.isError()) {
            logger.error("Error searching products: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getErrorMessage());
        }

        return ResponseEntity.ok(response.getJson());
    }

    /**
     * Get information about a specific product
     */
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductInfo(@PathVariable UUID productId) {
        logger.info("Received request to get product info for product ID: {}", productId);

        try {
            return ResponseEntity.ok(productService.getProductInfo(productId));
        } catch (Exception e) {
            logger.error("Error fetching product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found: " + e.getMessage());
        }
    }

    /**
     * Rate a product (requires authentication)
     */
    @PostMapping("/rate")
    public ResponseEntity<?> rateProduct(
            @RequestHeader("Authorization") String authToken,
            @RequestBody ProductRateRequest request) {

        logger.info("Received request to rate product {} by user {}",
                request.getProductId(), request.getUsername());

        String token = extractToken(authToken);
        Response response = productService.rateProduct(token, request);

        if (response.isError()) {
            logger.error("Error rating product: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getErrorMessage());
        }

        return ResponseEntity.ok(response.getJson());
    }

    /**
     * Add a new product to a store (requires authentication and authorization)
     */
    @PostMapping("/store/{storeId}")
    public ResponseEntity<?> addProductToStore(
            @RequestHeader("Authorization") String authToken,
            @PathVariable UUID storeId,
            @RequestBody ProductRequest productRequest,
            @RequestParam int quantity,
            @RequestParam String username) {

        logger.info("Received request to add product to store {} by user {}",
                storeId, username);

        String token = extractToken(authToken);
        Response response = productService.addProduct(username, token, productRequest, storeId, quantity);

        if (response.isError()) {
            logger.error("Error adding product: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getErrorMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response.getJson());
    }

    /**
     * Update an existing product (requires authentication and authorization)
     */
    @PutMapping("/store/{storeId}")
    public ResponseEntity<?> updateProduct(
            @RequestHeader("Authorization") String authToken,
            @PathVariable UUID storeId,
            @RequestBody ProductRequest productRequest,
            @RequestParam int quantity,
            @RequestParam String username) {

        logger.info("Received request to update product {} in store {} by user {}",
                productRequest.getProductId(), storeId, username);

        String token = extractToken(authToken);
        Response response = productService.updateProduct(username, token, storeId, productRequest, quantity);

        if (response.isError()) {
            logger.error("Error updating product: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getErrorMessage());
        }

        return ResponseEntity.ok(response.getJson());
    }

    /**
     * Delete a product from a store (requires authentication and authorization)
     */
    @DeleteMapping("/store/{storeId}")
    public ResponseEntity<?> deleteProduct(
            @RequestHeader("Authorization") String authToken,
            @PathVariable UUID storeId,
            @RequestBody ProductRequest productRequest,
            @RequestParam String username) {

        logger.info("Received request to delete product {} from store {} by user {}",
                productRequest.getProductId(), storeId, username);

        String token = extractToken(authToken);
        Response response = productService.deleteProduct(username, token, productRequest, storeId);

        if (response.isError()) {
            logger.error("Error deleting product: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getErrorMessage());
        }

        return ResponseEntity.ok(response.getJson());
    }

    /**
     * Get all products for a specific store
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getStoreProducts(@PathVariable UUID storeId) {
        logger.info("Received request to get all products for store ID: {}", storeId);

        Response response = productService.getStoreProducts(storeId);

        if (response.isError()) {
            logger.error("Error fetching store products: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getErrorMessage());
        }

        return ResponseEntity.ok(response.getJson());
    }

    /**
     * Helper method to extract the token from the Authorization header
     */
    private String extractToken(String authHeader) {
        // Assuming Bearer token format: "Bearer <token>"
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }
}