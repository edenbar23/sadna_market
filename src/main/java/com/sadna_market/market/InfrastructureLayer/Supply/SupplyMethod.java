package com.sadna_market.market.InfrastructureLayer.Supply;

public interface SupplyMethod {
    boolean accept(SupplyVisitor visitor, String address, double weight);
}

