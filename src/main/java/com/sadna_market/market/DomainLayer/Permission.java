package com.sadna_market.market.DomainLayer;

/**
 * Enum representing permissions in the market system.
 * Each permission controls a specific action that can be performed by users with different roles.
 * The numeric codes are used for easy identification and comparison. 
 * for example, 100's are for basic access permissions, 200's are for inventory management, and so on.
 */

public enum Permission {
    // Basic access permissions
    VIEW_STORE_INFO(100),
    VIEW_PRODUCT_INFO(101),
    
    // Inventory management permissions
    MANAGE_INVENTORY(200),
    ADD_PRODUCT(201),
    REMOVE_PRODUCT(202),
    UPDATE_PRODUCT(203),
    
    // Purchase policy management
    MANAGE_PURCHASE_POLICY(300),
    
    // Discount policy management
    MANAGE_DISCOUNT_POLICY(400),
    
    // Store personnel management
    APPOINT_STORE_OWNER(500),
    REMOVE_STORE_OWNER(501),
    APPOINT_STORE_MANAGER(502),
    REMOVE_STORE_MANAGER(503),
    UPDATE_MANAGER_PERMISSIONS(504),
    
    // Store operations permissions
    CLOSE_STORE(600),
    REOPEN_STORE(601),
    
    // Customer service permissions
    VIEW_STORE_PURCHASE_HISTORY(700),
    RESPOND_TO_USER_INQUIRIES(701),
    
    // Bidding system permissions
    RESPOND_TO_BID(800),
    
    // Auction and lottery management
    MANAGE_AUCTIONS(900),
    MANAGE_LOTTERIES(901);
    
    private final int code;
    
    Permission(int code) {
        this.code = code;
    }
    
    /**
     * Gets the numeric code associated with this permission
     * 
     * @return The permission code
     */
    public int getCode() {
        return code;
    }
    
    /**
     * Converts a numeric code to the corresponding Permission enum
     * 
     * @param code The numeric permission code
     * @return The corresponding Permission enum value
     * @throws IllegalArgumentException if no permission matches the given code
     */
    public static Permission fromCode(int code) {
        for (Permission permission : Permission.values()) {
            if (permission.getCode() == code) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Invalid permission code: " + code);
    }
}

