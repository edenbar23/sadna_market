package com.sadna_market.market.ApplicationLayer.Requests;

import java.util.Set;
import java.util.UUID;

import com.sadna_market.market.DomainLayer.Permission;

public class AppointManagerRequest {
    private String appointingUserName;
    private String newManagerUserName;
    private UUID storeId;
    private Set<Permission> permissions;

    public String getAppointingUserName() {
        return appointingUserName;
    }

    public void setAppointingUserName(String appointingUserName) {
        this.appointingUserName = appointingUserName;
    }

    public String getNewManagerUserName() {
        return newManagerUserName;
    }

    public void setNewManagerUserName(String newManagerUserName) {
        this.newManagerUserName = newManagerUserName;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}