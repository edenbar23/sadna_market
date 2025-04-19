package com.sadna_market.market.DomainLayer;

import java.util.List;

/**
 * Interface representing a user role in the market system.
 * Different roles have different permissions and capabilities within the system.
 * This interface defines the common behaviors and properties of all roles.
 */
public interface IUserRole {
    
    /**
     * Gets the type of the role
     * 
     * @return The RoleType enum representing this role
     */
    RoleType getRoleType();
    
    /**
     * Gets the username of the user who has this role
     * 
     * @return The username string
     */
    String getUsername();
    
    /**
     * Gets the store ID this role is associated with
     * 
     * @return The store ID
     */
    int getStoreId();
    
    /**
     * Checks if the role has a specific permission
     * 
     * @param permission The permission to check
     * @return true if the role has the permission, false otherwise
     */
    boolean hasPermission(Permission permission);
    
    /**
     * Adds a permission to this role
     * 
     * @param permission The permission to add
     */
    void addPermission(Permission permission);
    
    /**
     * Removes a permission from this role
     * 
     * @param permission The permission to remove
     */
    void removePermission(Permission permission);
    
    /**
     * Gets all permissions associated with this role
     * 
     * @return List of all permissions this role has
     */
    List<Permission> getPermissions();
    
    /**
     * Gets the username of the user who appointed this role
     * 
     * @return The appointee's username or null if not applicable (e.g., for founders)
     */
    String getAppointedBy();
    
    /**
     * Checks if this role was appointed by a specific user
     * 
     * @param username The username to check
     * @return true if the user appointed this role, false otherwise
     */
    boolean isAppointedByUser(String username);
    
    /**
     * Gets the list of users who were appointed by this role
     * 
     * @return List of usernames appointed by this role
     */
    List<String> getAppointees();
    
    /**
     * Adds an appointee to this role's list of appointed users
     * 
     * @param appointeeUsername The username of the appointee to add
     */
    void addAppointee(String appointeeUsername);
    
    /**
     * Accepts a visitor to perform role-specific operations
     * 
     * @param visitor The visitor to accept
     * @param storeId The store ID
     * @param member The member associated with this role
     */
    //TODO: Implement the visitor pattern for user roles after we have StoreFounder, StoreOwner, and StoreManager classes
    //void accept(UserRoleVisitor visitor, int storeId, Member member);
    
    /**
     * Creates a request for operations related to this role
     * 
     * @param senderName The username of the sender
     * @param sentName The username of the recipient
     * @param reqType The type of request
     * @return The request ID
     */
    String createRequest(String senderName, String sentName, String reqType);
}
