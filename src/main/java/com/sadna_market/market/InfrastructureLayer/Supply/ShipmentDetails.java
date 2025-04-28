package com.sadna_market.market.InfrastructureLayer.Supply;

public class ShipmentDetails {
    private String shipmentId;
    private String address;
    private int quantity;
    private String username;
    private boolean isGuest;

    public ShipmentDetails(String shipmentId, String address, int quantity, String username, boolean isGuest) {
        this.shipmentId = shipmentId;
        this.address = address;
        this.quantity = quantity;
        this.username = username;
        this.isGuest = isGuest;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public String getAddress() {
        return address;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUsername() {
        return username;
    }

    public boolean isGuest() {
        return isGuest;
    }

    @Override
    public String toString() {
        return "ShipmentDetails{" +
                "shipmentId='" + shipmentId + '\'' +
                ", address='" + address + '\'' +
                ", quantity=" + quantity +
                ", username='" + username + '\'' +
                ", isGuest=" + isGuest +
                '}';
    }
}