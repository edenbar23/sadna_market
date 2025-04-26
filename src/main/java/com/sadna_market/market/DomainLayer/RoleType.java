package com.sadna_market.market.DomainLayer;

/**
 * Enum representing the possible user role types in the market system.
 * These roles define a user's position and capabilities within the system.
 */

public enum RoleType {
    GUEST(1),           // An unauthenticated visitor
    STORE_FOUNDER(2),   // The original creator of a store
    STORE_OWNER(3),     // An owner of a store (can be multiple)
    STORE_MANAGER(4);   // A manager of a store with specific permissions
    
    private final int value;
    
    RoleType(int value) {
        this.value = value;
    }
    
    /**
     * Gets the numeric value associated with this role type
     * 
     * @return The role type value
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Converts a numeric value to the corresponding RoleType enum
     * 
     * @param value The numeric role type value
     * @return The corresponding RoleType enum value
     * @throws IllegalArgumentException if no role type matches the given value
     */
    public static RoleType fromValue(int value) {
        for (RoleType roleType : RoleType.values()) {
            if (roleType.getValue() == value) {
                return roleType;
            }
        }
        throw new IllegalArgumentException("Invalid role type value: " + value);
    }
}
