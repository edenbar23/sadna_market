package com.sadna_market.market.ApplicationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;



    public class Bridge {
        private ProductService productService;
        private UserService userService;
        private StoreService storeService;
        private ObjectMapper objectMapper = new ObjectMapper();


    }