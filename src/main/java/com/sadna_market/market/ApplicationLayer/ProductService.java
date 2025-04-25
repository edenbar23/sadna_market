package com.sadna_market.market.ApplicationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.DomainLayer.IProductRepository;
import com.sadna_market.market.DomainLayer.Product.Product;
import com.sadna_market.market.DomainLayer.Product.ProductDTO;
import com.sadna_market.market.DomainLayer.Product.UserRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final IProductRepository productRepository;

    public ProductService(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

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

    public Response searchProduct(ProductSearchRequest request) {
        logger.info("Computing intersection of filtered products with criteria - name: {}, category: {}, price: {} to {}, rate: {} to {}",
                request.getName(), request.getCategory(), request.getMinPrice(), request.getMaxPrice(),
                request.getMinRank(), request.getMaxRank());
        // convert to Response
        try {
            List<Optional<Product>> result = getProductsByParameters(request);

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


    // private helper method
    private List<Optional<Product>> getProductsByParameters(ProductSearchRequest request){
        // Get results from each filter method
        List<Optional<Product>> nameFiltered = productRepository.filterByName(request.getName());
        List<Optional<Product>> categoryFiltered = productRepository.filterByCategory(request.getCategory());
        List<Optional<Product>> priceFiltered = productRepository.filterByPriceRange(request.getMinPrice(), request.getMaxPrice());
        List<Optional<Product>> rateFiltered = productRepository.filterByRate(request.getMinRank(), request.getMaxRank());

        // Extract product IDs from each result set
        Set<UUID> nameIds = nameFiltered.stream()
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getProductId())
                .collect(Collectors.toSet());

        Set<UUID> categoryIds = categoryFiltered.stream()
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getProductId())
                .collect(Collectors.toSet());

        Set<UUID> priceIds = priceFiltered.stream()
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getProductId())
                .collect(Collectors.toSet());

        Set<UUID> rateIds = rateFiltered.stream()
                .filter(Optional::isPresent)
                .map(opt -> opt.get().getProductId())
                .collect(Collectors.toSet());

        // Compute intersection of all ID sets
        Set<UUID> intersectionIds = new HashSet<>(nameIds);
        intersectionIds.retainAll(categoryIds);
        intersectionIds.retainAll(priceIds);
        intersectionIds.retainAll(rateIds);

        // Get the final list of products from the intersection IDs
        return productRepository.getProductsByIds(intersectionIds);
    }

    public Response rateProduct(ProductRateRequest rate){
        UUID productId = rate.getProductId();
        UUID userId = rate.getUserId();
        int rateValue = rate.getRate();
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

    public Response addProductReview(ProductReviewRequest review) {
        UUID productId = review.getProductId();
        UUID userId = review.getUserId();
        String reviewText = review.getReviewText();
        logger.info("User {} added review for product {}: {}", userId, productId, reviewText);
        try {
            productRepository.handleUserReview(userId, productId, reviewText);
            return Response.success("Review added successfully");
        } catch (Exception e) {
            logger.error("Error adding review: {}", e.getMessage(), e);
            return Response.error("Error adding review: " + e.getMessage());
        }
    }
}
