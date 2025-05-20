package com.sadna_market.market.PresentationLayer.Controllers;

import com.sadna_market.market.ApplicationLayer.DTOs.ProductDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.ProductRatingDTO;
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

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for product-related operations.
 * Provides endpoints for searching, viewing, and managing products.
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<Response<List<ProductDTO>>> searchProducts(@RequestBody ProductSearchRequest request) {
        logger.info("Received request to search products");
        Response<List<ProductDTO>> response = productService.searchProduct(request);

        if (response.isError()) {
            logger.error("Error searching products: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get information about a specific product
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Response<ProductDTO>> getProductInfo(@PathVariable UUID productId) {
        logger.info("Received request to get product info for product ID: {}", productId);

        Response<ProductDTO> response = productService.getProductInfo(productId);

        if (response.isError()) {
            logger.error("Error fetching product: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Rate a product (requires authentication)
     */
    @PostMapping("/rate")
    public ResponseEntity<Response<ProductRatingDTO>> rateProduct(
            @RequestHeader("Authorization") String token,
            @RequestBody ProductRateRequest request) {

        logger.info("Received request to rate product {} by user {}",
                request.getProductId(), request.getUsername());

        Response<ProductRatingDTO> response = productService.rateProduct(token, request);

        if (response.isError()) {
            logger.error("Error rating product: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Add a new product to a store (requires authentication and authorization)
     */
    @PostMapping("/store/{storeId}")
    public ResponseEntity<Response<String>> addProductToStore(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID storeId,
            @RequestBody ProductRequest productRequest,
            @RequestParam int quantity,
            @RequestParam String username) {

        logger.info("Received request to add product to store {} by user {}",
                storeId, username);

        Response<String> response = productService.addProduct(username, token, productRequest, storeId, quantity);

        if (response.isError()) {
            logger.error("Error adding product: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing product (requires authentication and authorization)
     */
    @PutMapping("/store/{storeId}")
    public ResponseEntity<Response<String>> updateProduct(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID storeId,
            @RequestBody ProductRequest productRequest,
            @RequestParam int quantity,
            @RequestParam String username) {

        logger.info("Received request to update product {} in store {} by user {}",
                productRequest.getProductId(), storeId, username);

        Response<String> response = productService.updateProduct(username, token, storeId, productRequest, quantity);

        if (response.isError()) {
            logger.error("Error updating product: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a product from a store (requires authentication and authorization)
     */
    @DeleteMapping("/store/{storeId}")
    public ResponseEntity<Response<String>> deleteProduct(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID storeId,
            @RequestBody ProductRequest productRequest,
            @RequestParam String username) {

        logger.info("Received request to delete product {} from store {} by user {}",
                productRequest.getProductId(), storeId, username);

        Response<String> response = productService.deleteProduct(username, token, productRequest, storeId);

        if (response.isError()) {
            logger.error("Error deleting product: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get all products for a specific store
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<Response<List<ProductDTO>>> getStoreProducts(@PathVariable UUID storeId) {
        logger.info("Received request to get all products for store ID: {}", storeId);

        Response<List<ProductDTO>> response = productService.getStoreProducts(storeId);

        if (response.isError()) {
            logger.error("Error fetching store products: {}", response.getErrorMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }
}