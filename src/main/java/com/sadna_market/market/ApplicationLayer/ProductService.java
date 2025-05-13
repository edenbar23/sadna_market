package com.sadna_market.market.ApplicationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.DomainLayer.DomainServices.InventoryManagementService;
import com.sadna_market.market.DomainLayer.IProductRepository;
import com.sadna_market.market.DomainLayer.Product.Product;
import com.sadna_market.market.DomainLayer.Product.ProductDTO;
import com.sadna_market.market.DomainLayer.Product.UserRate;
import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final AuthenticationBridge authentication;
    private final IProductRepository productRepository;
    private final InventoryManagementService inventoryManagementService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProductService(AuthenticationBridge authentication,
                          IProductRepository productRepository,
                          InventoryManagementService inventoryManagementService,
                          ObjectMapper objectMapper) {
        this.authentication = authentication;
        this.productRepository = productRepository;
        this.inventoryManagementService = inventoryManagementService;
        this.objectMapper = objectMapper;
    }

    //req 2.1 (a)
    public ProductDTO getProductInfo(UUID productId) {
        logger.info("Getting product info for product ID: {}", productId);
        Optional<Product> product_ = productRepository.findById(productId);
        if(product_.isPresent()){
            Product product = (Product) product_.get();
            return new ProductDTO(product);
        } else {
            logger.error("User not found");
            return null;
        }
    }

    //req 2.2
    public Response searchProduct(ProductSearchRequest request) {
        logger.info("Computing intersection of filtered products with criteria - name: {}, category: {}, price: {} to {}, rate: {} to {}",
                request.getName(), request.getCategory(), request.getMinPrice(), request.getMaxPrice(),
                request.getMinRank(), request.getMaxRank());
        // convert to Response
        try {
            List<Optional<Product>> result = productRepository.searchProduct(request);

            // Filter out empty Optionals and extract the products
            List<Product> products = result.stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            // Convert products list to JSON string
            String json = objectMapper.writeValueAsString(products);

            // Return success response with JSON
            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error while searching for products: {}", e.getMessage(), e);
            return Response.error("Failed to search products: " + e.getMessage());
        }
    }


    //req 3.3
    public Response addProductReview(String token,ProductReviewRequest review) {
        logger.info("Validating token for user with username: {}", review.getUsername());
        authentication.validateToken(review.getUsername(),token);
        UUID productId = review.getProductId();
        UUID userId = review.getUserId();
        String reviewText = review.getReviewText();
        logger.info("User {} added review for product {}: {}", review.getUserId(), review.getProductId(), review.getReviewText());
        try {
            productRepository.handleUserReview(userId, productId, reviewText);
            //should add review to user also
            return Response.success("Review added successfully");
        } catch (Exception e) {
            logger.error("Error adding review: {}", e.getMessage(), e);
            return Response.error("Error adding review: " + e.getMessage());
        }
    }

    //req 3.4 (a)
    public Response rateProduct(String token, ProductRateRequest rate){
        logger.info("Validating token for user with username: {}", rate.getUsername());
        authentication.validateToken(rate.getUsername(),token);
        UUID productId = rate.getProductId();
        UUID userId = rate.getUserId();
        int rateValue = rate.getRating();
        logger.info("User {} rated product {} with value {}", userId, productId, rateValue);
        try {
            Optional<UserRate> userRateOptional = productRepository.handleUserRate(userId, productId, rateValue);

            if (userRateOptional.isPresent()) {
                UserRate userRate = userRateOptional.get();
                // Convert the UserRate to JSON or any format you're using for response
                // This part depends on how you want to serialize your objects
                // You might be using Jackson, Gson, or a custom serializer
                String json = objectMapper.writeValueAsString(userRate);

                return Response.success(json);
            } else {
                // If the Optional is empty, it likely means the product or user wasn't found
                return Response.error("Failed to rate product: Product or user not found");
            }
        } catch (Exception e) {
            logger.error("Error rating product: {}", e.getMessage(), e);
            return Response.error("Error rating product: " + e.getMessage());
        }    }

    public Response addProduct(String username, String token, ProductRequest product, UUID storeId, int quantity) {
        logger.info("Validating token for user with username: {}", username);
        authentication.validateToken(username, token);
        try {
            logger.info("Adding new product: {}", product);
            inventoryManagementService.addProductToStore(username,storeId,product,quantity);
            productRepository.addProduct(storeId, product.getName(), product.getCategory(), product.getDescription(),  product.getPrice(), true);
            return Response.success("Product added successfully");
        } catch (Exception e) {
            logger.error("Error adding product: {}", e.getMessage(), e);
            return Response.error("Error adding product: " + e.getMessage());
        }
    }

    //req 4.1 (c)
    // if quantity == -1 it means that we don't want to change it
    public Response updateProduct(String username, String token, UUID storeId, ProductRequest product, int quantity) {
        logger.info("Updating product: {}", product);
        authentication.validateToken(username, token);
        try {
            inventoryManagementService.updateProductInStore(username, storeId, product, quantity);
            productRepository.updateProduct(product);
            return Response.success("Product updated successfully");
        } catch (Exception e){
            logger.error("Error updating product: {}", e.getMessage(), e);
            return Response.error("Error updating product: " + e.getMessage());
            }
    }
    public Response deleteProduct(String username, String token, ProductRequest product, UUID storeId) {
        logger.info("Deleting product: {}", product);
        authentication.validateToken(username, token);
        logger.info("Validating token for user with username: {}", username);
        UUID productId = product.getProductId();

        if (product.getProductId() == null){
            logger.error("Product ID should not be null for existing products");
            return Response.error("Product ID should not be null for existing products");
        }
        try {
            inventoryManagementService.removeProductFromStore(username, storeId, product.getProductId());
            productRepository.deleteProduct(product);
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
            // Convert products list to JSON string
            String json = objectMapper.writeValueAsString(products);
            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error while getting store products: {}", e.getMessage(), e);
            return Response.error("Failed to get store products: " + e.getMessage());
        }
    }
    // returns all products for a specific store with a specific request (parameters and values)
    public Response getStoreProductsWithRequest(UUID storeId, ProductSearchRequest request) {
        logger.info("Getting products for store ID: {} with request: {}", storeId, request);
        try {
            List<Optional<Product>> products = productRepository.filterByStoreWithRequest(storeId, request);
            // Convert products list to JSON string
            String json = objectMapper.writeValueAsString(products);
            return Response.success(json);
        } catch (Exception e) {
            logger.error("Error while getting store products with request: {}", e.getMessage(), e);
            return Response.error("Failed to get store products with request: " + e.getMessage());
        }
    }

//    public void addRate(RateRequest rate) {
//    }


    public void clear() {
        productRepository.clear();
    }
}
