package com.sadna_market.market.DomainLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract base class for all user roles in the market system.
 * Implements common functionality defined in the IUserRole interface.
 */
public abstract class UserRole implements IUserRole {
    protected String username;
    protected int storeId;
    protected String appointedBy; // null for founder
    
    // Using thread-safe collections for concurrent access
    protected List<String> appointers; // users appointed by this role
    protected Set<Permission> permissions;
    
    /**
     * Constructor for creating a new role
     * 
     * @param username The username of the user with this role
     * @param storeId The store ID this role is associated with
     * @param appointedBy The username of the user who appointed this role (null for founder)
     */
    public UserRole(String username, int storeId, String appointedBy) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        this.username = username;
        this.storeId = storeId;
        this.appointedBy = appointedBy;
        this.appointers = new CopyOnWriteArrayList<>();
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
    public List<String> getAppointers() {
        return new ArrayList<>(appointers);
    }
    
    @Override
    public void addAppointer(String appointeeUsername) {
        if (appointeeUsername != null && !appointeeUsername.isEmpty() && 
            !appointers.contains(appointeeUsername)) {
            appointers.add(appointeeUsername);
        }
    }
    
    // TODO: Uncomment and implement this method in subclasses
    //public abstract void accept(UserRoleVisitor visitor, int storeId, Member member);
    
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
        
        UserRole other = (UserRole) obj;
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