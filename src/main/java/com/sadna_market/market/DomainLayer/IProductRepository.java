package com.sadna_market.market.DomainLayer;

import com.sadna_market.market.ApplicationLayer.Requests.ProductRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ProductSearchRequest;
import com.sadna_market.market.DomainLayer.Product.Product;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.sadna_market.market.DomainLayer.Product.UserRate;

public interface IProductRepository {
    Optional<Product> findById(UUID id);
    List<Optional<Product>> filterByName(String name);
    List<Optional<Product>> filterByCategory(String category);
    List<Optional<Product>> filterByPriceRange(double minPrice, double maxPrice);
    List<Optional<Product>> filterByRate(double minRate, double maxRate);
    void addProduct(UUID storeId, String name, String category, String description,double price, boolean isAvailable);
    void updateProduct(ProductRequest product);
    void deleteProduct(ProductRequest product);
    List<Optional<Product>> getProductsByIds(Set<UUID> intersectionIds);
    // New methods for store-specific operations
    List<Optional<Product>> findByStoreId(UUID storeId);
    List<Optional<Product>> filterByStoreWithRequest(UUID storeId, ProductSearchRequest request);
    Optional<UserRate> handleUserRate(UUID userId, UUID productId, int rateValue);
    void handleUserReview(UUID userId, UUID productId, String reviewText);
    List<Optional<Product>> searchProduct(ProductSearchRequest request);
}
