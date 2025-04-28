package com.sadna_market.market.InfrastructureLayer.Supply;

public class Main {
    public static void main(String[] args) {
        SupplyService service = new SupplyService();

        SupplyMethod standard = new StandardShippingDTO("FedEx", 3);
        SupplyMethod express = new ExpressShippingDTO("DHL", 1);
        SupplyMethod pickup = new PickupDTO("Central Store", "PICK123456");

        String address = "123 Main St, City, Country";
        double weight = 2.5; // kg

        service.ship(standard, address, weight);
        service.ship(express, address, weight);
        service.ship(pickup, address, weight);
    }
}