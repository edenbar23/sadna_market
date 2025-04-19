package com.sadna_market.market.DomainLayer;

import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Cart {
    private static final Logger logger = LogManager.getLogger(Cart.class);
    private int cartID; //Cart ID
    private HashMap<Integer,ShoppingBasket> shoppingBaskets; //Dictionary of shopping baskets, where the key is the storeID

    public Cart() {
        this.shoppingBaskets = new HashMap<>();
    }

    private boolean isStoreInCart(int storeID) {
        return shoppingBaskets.containsKey(storeID);
    }

    public void addToCart(int storeID, int productID, int quantity) {
        if (isStoreInCart(storeID)) {
            shoppingBaskets.get(storeID).addProduct(productID, quantity);
        }
        else {
            ShoppingBasket newShoppingBasket = new ShoppingBasket(storeID);
            newShoppingBasket.addProduct(productID, quantity);
            shoppingBaskets.put(storeID, newShoppingBasket);
        }
        logger.info("Product added to cart");

    }

    public void changeProductQuantity(int storeID, int productID, int quantity) {
        if (isStoreInCart(storeID)) {
            shoppingBaskets.get(storeID).changeProductQuantity(productID, quantity);
            logger.info("Product quantity changed in cart");
        }
        else {
            logger.error("Store not found in cart");
        }
    }

    public void removeFromCart(int storeID, int productID) {
        if (isStoreInCart(storeID)) {
            shoppingBaskets.get(storeID).removeProduct(productID);
            logger.info("Product removed from cart");
        }
        else {
            logger.error("Store not found in cart");
        }
    }

}
