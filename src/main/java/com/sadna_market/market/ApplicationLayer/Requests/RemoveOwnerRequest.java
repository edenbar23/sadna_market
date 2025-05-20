package com.sadna_market.market.ApplicationLayer.Requests;

import java.util.UUID;

public class RemoveOwnerRequest {
    private String founderUserName;
    private String removedOwnerUserName;
    private UUID storeId;

    public String getFounderUserName() {
        return founderUserName;
    }

    public void setFounderUserName(String founderUserName) {
        this.founderUserName = founderUserName;
    }

    public String getRemovedOwnerUserName() {
        return removedOwnerUserName;
    }

    public void setRemovedOwnerUserName(String removedOwnerUserName) {
        this.removedOwnerUserName = removedOwnerUserName;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

}
