package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductSearchRequest {
    private String name;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private Double minRank;
    private Double maxRank;
}
