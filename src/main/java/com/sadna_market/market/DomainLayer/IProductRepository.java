package com.sadna_market.market.DomainLayer;

import com.sadna_market.market.DomainLayer.Product.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IProductRepository {
    Optional<Product> findById(UUID id);
    List<Optional<Product>> filterByName(String name);
    List<Optional<Product>> filterByCategory(String category);
    List<Optional<Product>> filterByPriceRange(double minPrice, double maxPrice);
    List<Optional<Product>> filterByRate(double minRate, double maxRate);
    void addProduct(Product product);
    void updateProduct(Product product);
    void deleteProduct(UUID id);

}
