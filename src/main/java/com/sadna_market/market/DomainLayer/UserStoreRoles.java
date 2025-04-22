package com.sadna_market.market.DomainLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract base class for all user roles in the market system.
 * Implements common functionality defined in the IUserRole interface.
 */
public abstract class UserStoreRoles implements IUserRole {
    private static final Logger logger = LogManager.getLogger(UserStoreRoles.class);
    protected String username;
    protected int storeId;
    protected String appointedBy; // null for founder
    
    // Using thread-safe collections for concurrent access
    protected List<String> appointees; // users appointed by this role
    protected Set<Permission> permissions;
    
    /**
     * Constructor for creating a new role
     * 
     * @param username The username of the user with this role
     * @param storeId The store ID this role is associated with
     * @param appointedBy The username of the user who appointed this role (null for founder)
     */
    public UserStoreRoles(String username, int storeId, String appointedBy) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        this.username = username;
        this.storeId = storeId;
        this.appointedBy = appointedBy;
        this.appointees = new CopyOnWriteArrayList<>();
        this.permissions = ConcurrentHashMap.newKeySet();
        initializePermissions(); // Initialize role-specific permissions
    }
    
    /**
     * Method to be implemented by subclasses to set initial permissions
     * This allows each role type to have its own default set of permissions
     */
    protected abstract void initializePermissions();
    
    /**
     * Abstract method to get the role type of this role
     * Each concrete role class must implement this to specify its type
     * 
     * @return The RoleType enum value for this role
     */
    @Override
    public abstract RoleType getRoleType();
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public int getStoreId() {
        return storeId;
    }
    
    @Override
    public boolean hasPermission(Permission permission) {
        if (permission == null) {
            return false;
        }
        return permissions.contains(permission);
    }
    
    @Override
    public void addPermission(Permission permission) {
        if (permission != null) {
            permissions.add(permission);
        }
    }
    
    @Override
    public void removePermission(Permission permission) {
        if (permission != null) {
            permissions.remove(permission);
        }
    }
    
    @Override
    public List<Permission> getPermissions() {
        return new ArrayList<>(permissions);
    }
    
    @Override
    public String getAppointedBy() {
        return appointedBy;
    }
    
    @Override
    public boolean isAppointedByUser(String username) {
        if (username == null) {
            return false;
        }
        return appointedBy != null && appointedBy.equals(username);
    }
    
    @Override
    public List<String> getAppointees() {
        return new ArrayList<>(appointees);
    }
    
    @Override
    public void addAppointee(String appointeeUsername) {
        if (appointeeUsername != null && !appointeeUsername.isEmpty() && 
            !appointees.contains(appointeeUsername)) {
            appointees.add(appointeeUsername);
        }
    }
    
    /**
     * Abstract method to process the removal of this role
     * Each concrete role implementation delegates to the appropriate method on the visitor
     * 
     * @param visitor The visitor to handle the role-specific removal process
     * @param user The user with this role
     */
    @Override
    public abstract void processRoleRemoval(UserRoleVisitor visitor, User user);
    
    //TODO: Implement the createRequest method
    @Override
    public String createRequest(String senderName, String sentName, String reqType) {
        if (senderName == null || sentName == null || reqType == null) {
            throw new IllegalArgumentException("Request parameters cannot be null");
        }
        return null; // Placeholder for request creation logic
    }
    
    @Override
    public String toString() {
        return String.format("UserRole[type=%s, username=%s, storeId=%d, appointedBy=%s]", 
                            getRoleType(), username, storeId, appointedBy);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        UserStoreRoles other = (UserStoreRoles) obj;
        return username.equals(other.username) && 
               storeId == other.storeId && 
               getRoleType() == other.getRoleType();
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + username.hashCode();
        result = 31 * result + storeId;
        result = 31 * result + getRoleType().hashCode();
        return result;
    }
}