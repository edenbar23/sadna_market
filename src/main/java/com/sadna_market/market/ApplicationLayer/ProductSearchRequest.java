package com.sadna_market.market.ApplicationLayer;

public class ProductSearchRequest {
    String name;
    String category;
    double minPrice;
    double maxPrice;
    double minRank;
    double maxRank;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public double getMinPrice() {
        return minPrice;
    }
    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }
    public double getMaxPrice() {
        return maxPrice;
    }
    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }
    public double getMinRank() {
        return minRank;
    }
    public void setMinRank(double minRank) {
        this.minRank = minRank;
    }
    public double getMaxRank() {
        return maxRank;
    }
    public void setMaxRank(double maxRank) {
        this.maxRank = maxRank;
    }
}
