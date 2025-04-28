package com.sadna_market.market.DomainLayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

    public class Cart {
        private static final Logger logger = LogManager.getLogger(Cart.class);
        private final Map<UUID, ShoppingBasket> shoppingBaskets;

        public Cart() {
            this.shoppingBaskets = new HashMap<>();
        }

        public Cart(Map<UUID, Map<UUID,Integer>> shoppingBaskets) {
            //should validate storeId and productId before creating the cart
            this.shoppingBaskets = new HashMap<>();
            for (Map.Entry<UUID, Map<UUID, Integer>> entry : shoppingBaskets.entrySet()) {
                UUID storeId = entry.getKey();
                Map<UUID, Integer> products = entry.getValue();
                ShoppingBasket basket = new ShoppingBasket(storeId);
                for (Map.Entry<UUID, Integer> productEntry : products.entrySet()) {
                    basket.addProduct(productEntry.getKey(), productEntry.getValue());
                }
                this.shoppingBaskets.put(storeId, basket);
            }
        }

        public Cart addToCart(UUID storeId, UUID productId, int quantity) {
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }

            ShoppingBasket basket = shoppingBaskets.computeIfAbsent(storeId,
                    id -> new ShoppingBasket(storeId));
            basket.addProduct(productId, quantity);

            logger.info("Added {} units of product {} to store {} basket",
                    quantity, productId, storeId);
            return this;
        }

        public Cart removeFromCart(UUID storeId, UUID productId) {
            ShoppingBasket basket = shoppingBaskets.get(storeId);
            if (basket != null) {
                basket.removeProduct(productId);
                // Remove empty baskets
                if (basket.isEmpty()) {
                    shoppingBaskets.remove(storeId);
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
            ShoppingBasket basket = shoppingBaskets.get(storeId);
            if (basket != null) {
                basket.changeProductQuantity(productId, quantity);
            } else {
                logger.warn("No basket found for store {} when trying to change quantity", storeId);
            }
            return this;
        }

        public Map<UUID, ShoppingBasket> getShoppingBaskets() {
            return new HashMap<>(shoppingBaskets); // Return a defensive copy
        }

        public boolean isEmpty() {
            return shoppingBaskets.isEmpty();
        }

        public int getTotalItems() {
            return shoppingBaskets.values().stream()
                    .mapToInt(basket -> basket.getProductsList().values().stream()
                            .mapToInt(Integer::intValue).sum())
                    .sum();
        }
}
