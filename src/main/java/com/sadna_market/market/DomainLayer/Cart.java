package com.sadna_market.market.DomainLayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.sadna_market.market.ApplicationLayer.Requests.CartRequest;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor // Required by JPA
public class Cart {
    private static final Logger logger = LogManager.getLogger(Cart.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "cart_id")
    private UUID cartId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ShoppingBasket> shoppingBaskets = new ArrayList<>();

    @Setter
    @OneToOne(mappedBy = "cart")
    private User user;

    public Cart(Map<UUID, Map<UUID,Integer>> shoppingBaskets) {
        //should validate storeId and productId before creating the cart
        this.shoppingBaskets = new ArrayList<>();
        for (Map.Entry<UUID, Map<UUID, Integer>> entry : shoppingBaskets.entrySet()) {
            UUID storeId = entry.getKey();
            Map<UUID, Integer> products = entry.getValue();
            ShoppingBasket basket = new ShoppingBasket(storeId, this); // Use Cart-aware constructor
            for (Map.Entry<UUID, Integer> productEntry : products.entrySet()) {
                basket.addProduct(productEntry.getKey(), productEntry.getValue());
            }
            this.shoppingBaskets.add(basket);
        }
    }

    public Cart(CartRequest cartRequest) {
        this.shoppingBaskets = new ArrayList<>();
        for (Map.Entry<UUID, Map<UUID, Integer>> entry : cartRequest.getBaskets().entrySet()) {
            UUID storeId = entry.getKey();
            Map<UUID, Integer> products = entry.getValue();
            ShoppingBasket basket = new ShoppingBasket(storeId, this); // Use Cart-aware constructor
            for (Map.Entry<UUID, Integer> productEntry : products.entrySet()) {
                basket.addProduct(productEntry.getKey(), productEntry.getValue());
            }
            this.shoppingBaskets.add(basket);
        }
    }

    public Cart addToCart(UUID storeId, UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        ShoppingBasket basket = findOrCreateBasket(storeId);
        basket.addProduct(productId, quantity);

        logger.info("Added {} units of product {} to store {} basket",
                quantity, productId, storeId);
        return this;
    }

    public Cart removeFromCart(UUID storeId, UUID productId) {
        ShoppingBasket basket = findBasketByStoreId(storeId);
        if (basket != null) {
            basket.removeProduct(productId);
            // Remove empty baskets
            if (basket.isEmpty()) {
                shoppingBaskets.remove(basket);
                logger.info("Removed empty basket for store {}", storeId);
            }
        } else {
            logger.warn("No basket found for store {} when trying to remove product {}",
                    storeId, productId);
        }
        return this;
    }

    public Cart changeProductQuantity(UUID storeId, UUID productId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(storeId, productId);
            return this;
        }
        ShoppingBasket basket = findBasketByStoreId(storeId);
        if (basket != null) {
            basket.changeProductQuantity(productId, quantity);
        } else {
            logger.warn("No basket found for store {} when trying to change quantity", storeId);
        }
        return this;
    }

    // Helper method to find basket by store ID
    private ShoppingBasket findBasketByStoreId(UUID storeId) {
        return shoppingBaskets.stream()
                .filter(basket -> basket.getStoreId().equals(storeId))
                .findFirst()
                .orElse(null);
    }

    // Helper method to find or create basket
    private ShoppingBasket findOrCreateBasket(UUID storeId) {
        ShoppingBasket basket = findBasketByStoreId(storeId);
        if (basket == null) {
            basket = new ShoppingBasket(storeId, this); // Use Cart-aware constructor
            shoppingBaskets.add(basket);
        }
        return basket;
    }

    // Convert to Map format for backward compatibility
    public Map<UUID, ShoppingBasket> getShoppingBaskets() {
        Map<UUID, ShoppingBasket> basketMap = new HashMap<>();
        for (ShoppingBasket basket : shoppingBaskets) {
            basketMap.put(basket.getStoreId(), basket);
        }
        return basketMap; // Return a defensive copy
    }

    // Get the list of baskets (for JPA)
    public List<ShoppingBasket> getShoppingBasketsList() {
        return new ArrayList<>(shoppingBaskets);
    }

    public boolean isEmpty() {
        return shoppingBaskets.isEmpty();
    }

    public int getTotalItems() {
        return shoppingBaskets.stream()
                .mapToInt(basket -> basket.getProductsList().values().stream()
                        .mapToInt(Integer::intValue).sum())
                .sum();
    }

    // for testing
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ShoppingBasket basket : shoppingBaskets) {
            sb.append("Store ID: ").append(basket.getStoreId()).append("\n");
            sb.append("Products: ").append(basket.getProductsList()).append("\n");
        }
        return sb.toString();
    }

    public void addToShoppingBasket(ShoppingBasket basket) {
        if (basket == null) {
            throw new IllegalArgumentException("Basket cannot be null");
        }
        if(shoppingBaskets.contains(basket)) {
            //adding to this basket the products from basket:
            for(Map.Entry<UUID, Integer> entry : basket.getProductsList().entrySet()) {
                UUID productId = entry.getKey();
                int quantity = entry.getValue();
                this.addToCart(basket.getStoreId(), productId, quantity);
            }
        }
        else {
            shoppingBaskets.add(basket);
        }
        logger.info("Added shopping basket for store {}", basket.getStoreId());
    }
}