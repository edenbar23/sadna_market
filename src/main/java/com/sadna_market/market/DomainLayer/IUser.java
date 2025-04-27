package com.sadna_market.market.DomainLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

/**
 * This is an abstract class representing a user in the system.
 * It serves as a base class for different types of users (i,e User and Guest).
 */
abstract class IUser {
    private static final Logger logger = LogManager.getLogger(IUser.class);
    protected Cart cart;


    /**
     * Constructor for the IUser class.
     * Initializes the cart for the user.
     */
    public IUser() {
        this.cart = new Cart();
    }

    // Abstract method
    public abstract boolean isLoggedIn();

    // Regular method
    public void addProductToCart(UUID storeId, UUID productId, int amount) {
        logger.info("add amount of: {} of product id:{} of storeId: {}",amount,productId,storeId);
        cart.addToCart(storeId,productId, amount);
        logger.info("done add amount of: {} of product id:{} of StoreId: {}",amount,productId,storeId);
    }
    public void removeProductFromCart(UUID storeId,UUID productId) {
        logger.info("remove product id: {} of storeId: {}",productId,storeId);
        cart.removeFromCart(storeId, productId);
        logger.info("done remove product id: {} of storeId: {}",productId,storeId);

    }
    public void changeQuantityCart(UUID storeId,UUID productId, int amount) {
        logger.info("change amount of: {} of product id:{} of storeId: {}",amount,productId,storeId);
        cart.changeProductQuantity(storeId,productId, amount);
        logger.info("done change amount of: {} of product id:{} of storeId: {}",amount,productId,storeId);

    }
    public Cart getCart(){
        logger.info("get cart: {}",cart);
        return cart;
    }
}
