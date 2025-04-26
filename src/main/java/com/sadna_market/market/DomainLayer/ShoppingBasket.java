package com.sadna_market.market.DomainLayer;

import java.util.HashMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShoppingBasket {
    private static final Logger logger = LogManager.getLogger(ShoppingBasket.class);
    private HashMap <UUID, Integer> products; // Dictionary<productID,quantity>
    private UUID storeID; // Store ID
    //private int shoppingBasketID; // Shopping basket ID

    public ShoppingBasket(UUID storeID) {
        // Logic to initialize a shopping basket for a specific store
        // This could involve setting up a connection to the store's inventory, etc.
    }

    public void addProduct(UUID productID, int quantity) {
        // Logic to add a product to the shopping basket
        // This could involve checking if the product exists, updating the quantity, etc.
        if (products.containsKey(productID)) {
            logger.info("Product already exists in ShoppingBasket");
        } else {
            products.put(productID, quantity);
            logger.info("Product added to shopping basket");
        }
    }

    public void changeProductQuantity(UUID productID, int quantity) {
        // Logic to change the quantity of a product in the shopping basket
        // This could involve checking if the product exists, updating the quantity, etc.
        if (products.containsKey(productID)) {
            products.put(productID, quantity);
            logger.info("Product quantity changed in shopping basket");
        } else {
            logger.error("Product not found in shopping basket");
        }
    }

    public void removeProduct(UUID productID) {
        // Logic to remove a product from the shopping basket
        // This could involve checking if the product exists, updating the quantity, etc.
        if (products.containsKey(productID)) {
            products.remove(productID);
            logger.info("Product removed from shopping basket");
        } else {
            logger.error("Product not found in shopping basket");
        }
    }

    public HashMap<UUID,Integer> getProductsList() {
        // Logic to get the list of products in the shopping basket
        // This could involve returning a list of product IDs and quantities, etc.
        HashMap<UUID,Integer> productList = new HashMap<>();
        for (UUID productID : products.keySet()) {
            productList.put(productID, products.get(productID));
        }
        return productList;
    }
}
