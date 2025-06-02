package com.sadna_market.market.DomainLayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.*;

@Entity
@Table(name = "shopping_baskets")
@Getter
@NoArgsConstructor
public class ShoppingBasket {
    private static final Logger logger = LogManager.getLogger(ShoppingBasket.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "basket_id")
    private UUID basketId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "basket_products",
            joinColumns = @JoinColumn(name = "basket_id")
    )
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> products = new HashMap<>();

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    public ShoppingBasket(UUID storeId) {
        this.storeId = storeId;
        this.products = new HashMap<>();
    }


    ShoppingBasket(UUID storeId, Cart cart) {
        this.storeId = storeId;
        this.cart = cart;
        this.products = new HashMap<>();
    }

    public void addProduct(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        int currentQuantity = products.getOrDefault(productId, 0);
        int newQuantity = currentQuantity + quantity;
        products.put(productId, newQuantity);

        logger.info("Product {} quantity updated from {} to {} in basket for store {}",
                productId, currentQuantity, newQuantity, storeId);
    }

    public void changeProductQuantity(UUID productId, int quantity) {
        if (quantity <= 0) {
            removeProduct(productId);
        } else {
            products.put(productId, quantity);
            logger.info("Product {} quantity set to {} in basket for store {}",
                    productId, quantity, storeId);
        }
    }

    public void removeProduct(UUID productId) {
        if (products.remove(productId) != null) {
            logger.info("Product {} removed from basket for store {}", productId, storeId);
        } else {
            logger.warn("Product {} not found in basket for store {}", productId, storeId);
        }
    }

    public Map<UUID, Integer> getProductsList() {
        return new HashMap<>(products); // Return a defensive copy
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    public int getTotalQuantity() {
        return products.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean containsProduct(UUID productId) {
        return products.containsKey(productId);
    }

    public int getProductQuantity(UUID productId) {
        return products.getOrDefault(productId, 0);
    }

}