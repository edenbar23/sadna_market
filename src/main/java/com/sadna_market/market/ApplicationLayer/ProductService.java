package com.sadna_market.market.ApplicationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.DTOs.ProductDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.ProductRatingDTO;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.DomainLayer.DomainServices.InventoryManagementService;
import com.sadna_market.market.DomainLayer.DomainServices.RatingService;
import com.sadna_market.market.DomainLayer.IProductRepository;
import com.sadna_market.market.DomainLayer.Product;
import com.sadna_market.market.DomainLayer.ProductRating;
import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationBridge;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final AuthenticationBridge authentication;
    private final IProductRepository productRepository;
    private final InventoryManagementService inventoryManagementService;
    private final RatingService ratingService;
    private final ObjectMapper objectMapper;

    //req 2.1 (a)
    public ProductDTO getProductInfo(UUID productId) {
        logger.info("Getting product info for product ID: {}", productId);
        Optional<Product> product_ = productRepository.findById(productId);
        if(product_.isPresent()){
            Product product = product_.get();
            return new ProductDTO(product);
        } else {
            logger.error("Product not found");
            return null;
        }
    }

    //req 2.2
    public Response searchProduct(ProductSearchRequest request) {
        logger.info("Searching for products with criteria");
        try {
            // Convert application request to domain parameters
            List<Optional<Product>> result = productRepository.searchProduct(
                    request.getName(),
                    request.getCategory(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getMinRank(),
                    request.getMaxRank()
            );
            logger.info("products found: {}", result.toString());
            // Filter out empty Optionals and extract the products
            List<Product> products = result.stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            // Convert to DTOs for the application layer
            List<ProductDTO> productDTOs = products.stream()
                    .map(ProductDTO::new)
                    .collect(Collectors.toList());

            // Convert products list to JSON string
            String json = objectMapper.writeValueAsString(productDTOs);

            // Return success response with JSON
            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error while searching for products: {}", e.getMessage(), e);
            return Response.error("Failed to search products: " + e.getMessage());
        }
    }

    //req 3.3
    public Response addProductReview(String token, ProductReviewRequest review) {
        logger.info("Adding review for product ID: {}", review.getProductId());
        try {
            logger.info("Validating token for user with username: {}", review.getUsername());
            authentication.validateToken(review.getUsername(), token);

            // Convert application request to domain parameters
            productRepository.addProductReview(
                    review.getProductId(),
                    review.getUsername(),
                    review.getComment()
            );

            return Response.success("Review added successfully");
        } catch (Exception e) {
            logger.error("Error adding review: {}", e.getMessage(), e);
            return Response.error("Error adding review: " + e.getMessage());
        }
    }

    //req 3.4 (a)
    public Response rateProduct(String token, ProductRateRequest rate) {
        try {
            logger.info("Validating token for user with username: {}", rate.getUsername());
            authentication.validateToken(rate.getUsername(), token);

            logger.info("User {} rating product {} with value {}",
                    rate.getUsername(), rate.getProductId(), rate.getRating());

            // Convert application request to domain parameters
            ProductRating productRating = ratingService.rateProduct(
                    rate.getUsername(),
                    rate.getProductId(),
                    rate.getRating());

            // Convert domain object to DTO for response
            ProductRatingDTO ratingDTO = new ProductRatingDTO(productRating);
            String json = objectMapper.writeValueAsString(ratingDTO);

            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error rating product: {}", e.getMessage(), e);
            return Response.error("Error rating product: " + e.getMessage());
        }
    }

    public Response addProduct(String username, String token, ProductRequest product, UUID storeId, int quantity) {
        logger.info("Adding new product to store: {}", storeId);
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            // Convert application request to domain parameters
            UUID productId = inventoryManagementService.addProductToStore(
                    username,
                    storeId,
                    product.getName(),
                    product.getCategory(),
                    product.getDescription(),
                    product.getPrice(),
                    quantity
            );
            logger.info("Service layer: Product added with ID: {}", productId);
            //return Response.success(productId.toString());
            return Response.success(String.valueOf(productId));

        } catch (Exception e) {
            logger.error("Error adding product: {}", e.getMessage(), e);
            return Response.error("Error adding product: " + e.getMessage());
        }
    }

    //req 4.1 (c)
    public Response updateProduct(String username, String token, UUID storeId, ProductRequest product, int quantity) {
        logger.info("Updating product: {}", product.getProductId());
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            // Convert application request to domain parameters
            inventoryManagementService.updateProductInStore(
                    username,
                    storeId,
                    product.getProductId(),
                    product.getName(),
                    product.getDescription(),
                    product.getCategory(),
                    product.getPrice(),
                    quantity
            );

            return Response.success("Product updated successfully");
        } catch (Exception e){
            logger.error("Error updating product: {}", e.getMessage(), e);
            return Response.error("Error updating product: " + e.getMessage());
        }
    }

    public Response deleteProduct(String username, String token, ProductRequest product, UUID storeId) {
        logger.info("Deleting product: {}", product.getProductId());
        try {
            logger.info("Validating token for user with username: {}", username);
            authentication.validateToken(username, token);

            if (product.getProductId() == null){
                logger.error("Product ID should not be null for existing products");
                return Response.error("Product ID should not be null for existing products");
            }

            // Convert application request to domain parameters
            inventoryManagementService.removeProductFromStore(
                    username,
                    storeId,
                    product.getProductId()
            );

            return Response.success("Product deleted successfully");
        } catch (Exception e){
            logger.error("Error deleting product: {}", e.getMessage(), e);
            return Response.error("Error deleting product: " + e.getMessage());
        }
    }

    // returns all products for a specific store
    public Response getStoreProducts(UUID storeId) {
        logger.info("Getting products for store ID: {}", storeId);
        try {
            List<Optional<Product>> products = productRepository.findByStoreId(storeId);

            // Convert to DTOs
            List<ProductDTO> productDTOs = products.stream()
                    .filter(Optional::isPresent)
                    .map(p -> new ProductDTO(p.get()))
                    .collect(Collectors.toList());

            // Convert products list to JSON string
            String json = objectMapper.writeValueAsString(productDTOs);
            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error while getting store products: {}", e.getMessage(), e);
            return Response.error("Failed to get store products: " + e.getMessage());
        }
    }

    // returns all products for a specific store with search criteria
    public Response getStoreProductsWithRequest(UUID storeId, ProductSearchRequest request) {
        logger.info("Getting products for store ID: {} with request", storeId);
        try {
            // Convert application request to domain parameters
            List<Optional<Product>> products = productRepository.filterByStoreWithCriteria(
                    storeId,
                    request.getName(),
                    request.getCategory(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    request.getMinRank(),
                    request.getMaxRank()
            );

            // Convert to DTOs
            List<ProductDTO> productDTOs = products.stream()
                    .filter(Optional::isPresent)
                    .map(p -> new ProductDTO(p.get()))
                    .collect(Collectors.toList());

            // Convert to JSON string
            String json = objectMapper.writeValueAsString(productDTOs);
            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error while getting store products with request: {}", e.getMessage(), e);
            return Response.error("Failed to get store products with request: " + e.getMessage());
        }
    }

    public void clear() {
        productRepository.clear();
    }
}