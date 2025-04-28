package com.sadna_market.market.InfrastructureLayer.Supply;

public class SupplyService {
    private SupplyVisitor visitor = new ConcreteSupplyVisitor();

    public boolean ship(SupplyMethod method, String address, double weight) {
        return method.accept(visitor, address, weight);
    }
}