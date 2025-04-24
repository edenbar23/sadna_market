package com.sadna_market.market.ApplicationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.DomainLayer.IProductRepository;
import com.sadna_market.market.DomainLayer.Product.Product;
import com.sadna_market.market.DomainLayer.Product.ProductDTO;
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
        logger.debug("Computing intersection of filtered products with criteria - name: {}, category: {}, price: {} to {}, rate: {} to {}",
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


}
