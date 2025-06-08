package com.sadna_market.market.InfrastructureLayer.Adapters;


import com.sadna_market.market.ApplicationLayer.DTOs.ProductDTO;
import com.sadna_market.market.DomainLayer.IProductRepository;
import com.sadna_market.market.DomainLayer.Product;
import com.sadna_market.market.InfrastructureLayer.JpaRepos.ProductJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile({"dev", "prod", "default"})
public class ProductJpaAdapter implements IProductRepository {

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Override
    public Optional<Product> findById(UUID id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public List<Optional<Product>> filterByName(String name) {
        return productJpaRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<Optional<Product>> filterByCategory(String category) {
        return productJpaRepository.findByCategory(category)
                .stream()
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<Optional<Product>> filterByPriceRange(double minPrice, double maxPrice) {
        return productJpaRepository.findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<Optional<Product>> filterByRate(double minRate, double maxRate) {
        return productJpaRepository.findByRateRange(minRate, maxRate)
                .stream()
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<Optional<Product>> findAll() {
        return productJpaRepository.findAll()
                .stream()
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public UUID addProduct(UUID storeId, String name, String category, String description, double price, boolean isAvailable) {
        Product product = new Product(name, storeId, category, description, price, isAvailable);
        Product savedProduct = productJpaRepository.save(product);
        return savedProduct.getProductId();
    }

    @Override
    public void updateProduct(UUID productId, String name, String category, String description, double price) {
        Optional<Product> optionalProduct = productJpaRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.updateProduct(name, description, category, price);
            productJpaRepository.save(product);
        }
    }

    @Override
    public boolean deleteProductRating(UUID ratingId) {
        // This method seems to be related to a separate Rating entity
        // You'll need to implement this based on your rating system
        // For now, returning false as placeholder
        return false;
    }

    @Override
    public List<Optional<Product>> getProductsByIds(Set<UUID> productIds) {
        return productJpaRepository.findByProductIdIn(productIds)
                .stream()
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<Optional<Product>> findByStoreId(UUID storeId) {
        return productJpaRepository.findByStoreId(storeId)
                .stream()
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public List<Optional<Product>> filterByStoreWithCriteria(UUID storeId, String name, String category,
                                                             Double minPrice, Double maxPrice,
                                                             Double minRate, Double maxRate) {
        return productJpaRepository.findByStoreWithCriteria(storeId, name, category, minPrice, maxPrice, minRate, maxRate)
                .stream()
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProduct(UUID productId) {
        productJpaRepository.deleteById(productId);
    }

    @Override
    public void addProductRating(UUID productId, String username, int ratingValue) {
        Optional<Product> optionalProduct = productJpaRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.addRank(ratingValue);
            productJpaRepository.save(product);
        }
    }

    @Override
    public void updateProductRating(UUID productId, int oldRating, int newRating) {
        Optional<Product> optionalProduct = productJpaRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.updateRank(oldRating, newRating);
            productJpaRepository.save(product);
        }
    }

    @Override
    public void addProductReview(UUID productId, String username, String reviewText) {
        // This seems to be related to a separate Review entity
        // You'll need to implement this based on your review system
        // For now, this is a placeholder
    }

    @Override
    public List<Optional<Product>> searchProduct(String name, String category,
                                                 Double minPrice, Double maxPrice,
                                                 Double minRate, Double maxRate) {
        return productJpaRepository.searchProducts(name, category, minPrice, maxPrice, minRate, maxRate)
                .stream()
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        productJpaRepository.deleteAll();
    }

    @Override
    public List<ProductDTO> getTopRatedProducts(UUID storeId) {
        return productJpaRepository.findTopRatedByStore(storeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getTopTenRatedProducts() {
        return productJpaRepository.findTopTenRatedProducts()
                .stream()
                .limit(10)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProductDTO convertToDTO(Product product) {
        // You'll need to implement this based on your ProductDTO structure
        // This is a placeholder - adjust according to your DTO
        return new ProductDTO(
                product.getProductId(),
                product.getStoreId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.isAvailable(),
                product.getRate(),
                product.getNumOfRanks()
        );
    }
}