package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.Data;

import lombok.NoArgsConstructor;


/**
 * ProductSearchRequest is a class that represents a request to search for products.
 * It contains fields for the product name, category, price range, and rank range.
 * The class is annotated with @Data and @NoArgsConstructor from Lombok to generate boilerplate code.
 * If a field is not set (set as null in case of a String, and -1.0 in case of a double), it will be ignored in the search.
 */

@Data

public class ProductSearchRequest {
    private String name;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private Double minRank;
    private Double maxRank;

    public ProductSearchRequest(String name, String category, Double minPrice, Double maxPrice, Double minRank, Double maxRank) {
        this.name = name;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minRank = minRank;
        this.maxRank = maxRank;
    }
    public ProductSearchRequest() {
        this.name = null;
        this.category = null;
        this.minPrice = -1.0;
        this.maxPrice = -1.0;
        this.minRank = -1.0;
        this.maxRank = -1.0;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getCategory(){
        return category;
    }
    public void setCategory(String category){
        this.category = category;
    }
    public Double getMinPrice(){
        return minPrice;
    }
    public void setMinPrice(Double minPrice){
        this.minPrice = minPrice;
    }
    public Double getMaxPrice(){
        return maxPrice;
    }
    public void setMaxPrice(Double maxPrice){
        this.maxPrice = maxPrice;
    }
    public Double getMinRank(){
        return minRank;
    }
    public void setMinRank(Double minRank){
        this.minRank = minRank;
    }
    public Double getMaxRank(){
        return maxRank;
    }
    public void setMaxRank(Double maxRank){
        this.maxRank = maxRank;
    }

}
