package com.sadna_market.market.DomainLayer;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IProductRepository {
    Optional<Product> findById(UUID id);
    List<Optional<Product>> filterByName(String name);
    List<Optional<Product>> filterByCategory(String category);
    List<Optional<Product>> filterByPriceRange(double minPrice, double maxPrice);
    List<Optional<Product>> filterByRate(double minRate, double maxRate);

    UUID addProduct(UUID storeId, String name, String category, String description, double price, boolean isAvailable);

    void updateProduct(UUID productId, String name, String category, String description, double price);

    boolean deleteProductRating(UUID ratingId);

    List<Optional<Product>> getProductsByIds(Set<UUID> productIds);
    List<Optional<Product>> findByStoreId(UUID storeId);

    List<Optional<Product>> filterByStoreWithCriteria(UUID storeId, String name, String category,
                                                      Double minPrice, Double maxPrice,
                                                      Double minRate, Double maxRate);

    void deleteProduct(UUID productId);

    void addProductRating(UUID productId, String username, int ratingValue);


    void updateProductRating(UUID productId, int oldRating, int newRating);

    void addProductReview(UUID productId, String username, String reviewText);

    List<Optional<Product>> searchProduct(String name, String category,
                                          Double minPrice, Double maxPrice,
                                          Double minRate, Double maxRate);

    void clear();
}