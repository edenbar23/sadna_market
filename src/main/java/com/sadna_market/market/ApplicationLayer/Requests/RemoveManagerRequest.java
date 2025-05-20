package com.sadna_market.market.ApplicationLayer.Requests;

import java.util.UUID;

public class RemoveManagerRequest {
    private String appointingUserName;
    private String removedManagerUserName;
    private UUID storeId;

    public String getAppointingUserName() {
        return appointingUserName;
    }

    public void setAppointingUserName(String appointingUserName) {
        this.appointingUserName = appointingUserName;
    }

    public String getRemovedManagerUserName() {
        return removedManagerUserName;
    }

    public void setRemovedManagerUserName(String removedManagerUserName) {
        this.removedManagerUserName = removedManagerUserName;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }
}
