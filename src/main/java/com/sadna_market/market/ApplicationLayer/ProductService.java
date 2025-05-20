package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.ApplicationLayer.DTOs.ProductDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.ProductRatingDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.ProductReviewDTO;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.DomainServices.InventoryManagementService;
import com.sadna_market.market.DomainLayer.DomainServices.RatingService;
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

    //req 2.1 (a)
    public Response<ProductDTO> getProductInfo(UUID productId) {
        logger.info("Getting product info for product ID: {}", productId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            return Response.success(new ProductDTO(product));
        } else {
            logger.error("Product not found");
            return Response.error("Product not found with ID: " + productId);
        }
    }

    //req 2.2
    public Response<List<ProductDTO>> searchProduct(ProductSearchRequest request) {
        logger.info("Searching for products with criteria");
        try {
            List<Product> products = new ArrayList<>();

            // If no search criteria, get all products
            if (isEmptyRequest(request)) {
                logger.info("No search criteria provided, returning all products");
                List<Optional<Product>> allProducts = productRepository.findAll();
                for (Optional<Product> productOpt : allProducts) {
                    productOpt.ifPresent(products::add);
                }
            } else {
                // First try with specific search criteria
                List<Optional<Product>> searchResults = productRepository.searchProduct(
                        request.getName(),
                        request.getCategory(),
                        request.getMinPrice(),
                        request.getMaxPrice(),
                        request.getMinRank(),
                        request.getMaxRank()
                );

                logger.info("Search results size: {}", searchResults.size());

                // Extract actual products
                for (Optional<Product> productOpt : searchResults) {
                    if (productOpt.isPresent()) {
                        products.add(productOpt.get());
                    }
                }

                // If no results yet and name was provided, try a more flexible name search
                if (products.isEmpty() && request.getName() != null && !request.getName().isEmpty()) {
                    logger.info("No exact matches found, trying partial name match");
                    List<Optional<Product>> allProducts = productRepository.findAll();
                    String searchName = request.getName().toLowerCase();

                    for (Optional<Product> productOpt : allProducts) {
                        if (productOpt.isPresent()) {
                            Product product = productOpt.get();
                            if (product.getName().toLowerCase().contains(searchName)) {
                                products.add(product);
                            }
                        }
                    }
                }
            }

            logger.info("Final products found: {}", products.size());

            // Convert to DTOs for the application layer
            List<ProductDTO> productDTOs = products.stream()
                    .map(ProductDTO::new)
                    .collect(Collectors.toList());

            // Return success response with product DTOs
            return Response.success(productDTOs);
        } catch (Exception e) {
            logger.error("Error while searching for products: {}", e.getMessage(), e);
            return Response.error("Failed to search products: " + e.getMessage());
        }
    }

    private boolean isEmptyRequest(ProductSearchRequest request) {
        return (request.getName() == null || request.getName().isEmpty()) &&
                (request.getCategory() == null || request.getCategory().isEmpty()) &&
                (request.getMinPrice() == null) &&
                (request.getMaxPrice() == null) &&
                (request.getMinRank() == null) &&
                (request.getMaxRank() == null);
    }

    //req 3.3
    public Response<String> addProductReview(String token, ProductReviewRequest review) {
        logger.info("Adding review for product ID: {}", review.getProductId());
        try {
            logger.info("Validating token for user with username: {}", review.getUsername());
            authentication.validateToken(review.getUsername(), token);

            // Convert application request to domain parameters
            productRepository.addProductReview(
                    review.getProductId(),
                    review.getUsername(),
                    review.getReviewText()
            );

            return Response.success("Review added successfully");
        } catch (Exception e) {
            logger.error("Error adding review: {}", e.getMessage(), e);
            return Response.error("Error adding review: " + e.getMessage());
        }
    }

    //req 3.4 (a)
    public Response<ProductRatingDTO> rateProduct(String token, ProductRateRequest rate) {
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

            return Response.success(ratingDTO);
        } catch (Exception e) {
            logger.error("Error rating product: {}", e.getMessage(), e);
            return Response.error("Error rating product: " + e.getMessage());
        }
    }

    public Response<String> addProduct(String username, String token, ProductRequest product, UUID storeId, int quantity) {
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
            return Response.success(productId.toString());
        } catch (Exception e) {
            logger.error("Error adding product: {}", e.getMessage(), e);
            return Response.error("Error adding product: " + e.getMessage());
        }
    }

    //req 4.1 (c)
    public Response<String> updateProduct(String username, String token, UUID storeId, ProductRequest product, int quantity) {
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

    public Response<String> deleteProduct(String username, String token, ProductRequest product, UUID storeId) {
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
    public Response<List<ProductDTO>> getStoreProducts(UUID storeId) {
        logger.info("Getting products for store ID: {}", storeId);
        try {
            List<Optional<Product>> products = productRepository.findByStoreId(storeId);

            // Convert to DTOs
            List<ProductDTO> productDTOs = products.stream()
                    .filter(Optional::isPresent)
                    .map(p -> new ProductDTO(p.get()))
                    .collect(Collectors.toList());

            return Response.success(productDTOs);
        } catch (Exception e) {
            logger.error("Error while getting store products: {}", e.getMessage(), e);
            return Response.error("Failed to get store products: " + e.getMessage());
        }
    }

    // returns all products for a specific store with search criteria
    public Response<List<ProductDTO>> getStoreProductsWithRequest(UUID storeId, ProductSearchRequest request) {
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

            return Response.success(productDTOs);
        } catch (Exception e) {
            logger.error("Error while getting store products with request: {}", e.getMessage(), e);
            return Response.error("Failed to get store products with request: " + e.getMessage());
        }
    }

    public Response<ProductReviewDTO> reviewProduct(String username, String token, ProductReviewRequest review) {
        logger.info("Adding review for product ID: {}", review.getProductId());
        try {
            logger.info("Validating token for user with username: {}", review.getUsername());
            authentication.validateToken(review.getUsername(), token);

            // Convert application request to domain parameters
            ProductReview productReview = ratingService.reviewProduct(username,
                    review.getProductId(),
                    review.getStoreId(),
                    review.getReviewText());
            // Convert domain object to DTO for response
            ProductReviewDTO reviewDTO = new ProductReviewDTO(productReview);
            return Response.success(reviewDTO);
        } catch (Exception e) {
            logger.error("Error adding product review: {}", e.getMessage(), e);
            return Response.error("Error adding product review: " + e.getMessage());
        }
    }

    public void clear() {
        productRepository.clear();
    }

    public Response<List<ProductDTO>> getTopRatedProducts(UUID storeId) {
        logger.info("Getting top rated stores");

        try {
            // Get all stores from the repository
            List<ProductDTO> topRatedProducts = productRepository.getTopRatedProducts(storeId);
            return Response.success(topRatedProducts);

        } catch (Exception e) {
            logger.error("Error getting top rated stores: {}", e.getMessage(), e);
            return Response.error("Failed to get top rated stores: " + e.getMessage());
        }
    }
}