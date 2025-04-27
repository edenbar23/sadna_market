package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.DomainLayer.Permission;
import java.util.HashSet;
import java.util.Set;

/**
 * Request object for managing permissions.
 * Used when appointing managers or updating their permissions.
 */
public class PermissionsRequest {
    private Set<Permission> permissions;

    /**
     * Default constructor
     */
    public PermissionsRequest() {
        this.permissions = new HashSet<>();
    }

    /**
     * Constructor with permissions
     *
     * @param permissions Set of permissions to assign
     */
    public PermissionsRequest(Set<Permission> permissions) {
        this.permissions = permissions != null ? new HashSet<>(permissions) : new HashSet<>();
    }

    public Set<Permission> getPermissions() {
        return new HashSet<>(permissions);
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions != null ? new HashSet<>(permissions) : new HashSet<>();
    }

    public void addPermission(Permission permission) {
        if (permission != null) {
            this.permissions.add(permission);
        }
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        return this.permissions.contains(permission);
    }

    @Override
    public String toString() {
        return "PermissionsRequest{" +
                "permissions=" + permissions +
                '}';
    }
}