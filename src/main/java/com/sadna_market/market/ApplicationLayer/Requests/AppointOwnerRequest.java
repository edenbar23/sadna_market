package com.sadna_market.market.ApplicationLayer.Requests;

import java.util.UUID;

public class AppointOwnerRequest {
    private String founderUserName;
    private String newOwnerUserName;
    private UUID storeId;

    public String getFounderUserName() {
        return founderUserName;
    }

    public void setFounderUserName(String founderUserName) {
        this.founderUserName = founderUserName;
    }

    public String getNewOwnerUserName() {
        return newOwnerUserName;
    }

    public void setNewOwnerUserName(String newOwnerUserName) {
        this.newOwnerUserName = newOwnerUserName;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }
}
