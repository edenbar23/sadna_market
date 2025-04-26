package com.sadna_market.market.ApplicationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;



public class Bridge {
    private ProductService productService;
    private UserService userService;
    private StoreService storeService;
    private ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Store Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * Store component. These methods test the functionality of stores including inventory
     * management, product listings, pricing strategies, and store policies.
     */
    public Response addProductToStore() {
        //TODO
        return null;
    }
    public Response removeProductFromStore() {
        //TODO
        return null;
    }
    public Response editProductDetails() {
        //TODO
        return null;
    }

    public Response getProductsList(){
        //TODO
        return null;
    }
    public Response getRoles(){
        //TODO
        return null;
    }

    /**
     * Guest Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * Guest component. These methods test the functionality available to non-registered
     * users including browsing products, adding items to carts, and registering for
     * an account.
     */
    public Response signUp(){
        //TODO
        return null;
    }
    public Response login(){
        //TODO
        return null;
    }

    public Response addProductToGuestBasket(){
        //TODO
        return null;
    }

    public Response removeProductFromGuestBasket(){
        //TODO
        return null;
    }

    public Response addUnavailableProductToGuestBasket(){
        //TODO
        return null;
    }
    public Response removeUnavailableProductFromGuestBasket(){
        //TODO
        return null;
    }
    public Response buyGuestCart(){
        //TODO
        return null;
    }

    /**
     * User Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * User component. These methods test the functionality for registered users including
     * login/logout, profile management, order history, and user-specific permissions.
     */
    public Response addProductToMemberBasket(){
        //TODO
        return null;
    }

    public Response removeProductFromMemberBasket(){
        //TODO
        return null;
    }

    public Response addUnavailableProductToMemberBasket(){
        //TODO
        return null;
    }
    public Response removeUnavailableProductFromMemberBasket(){
        //TODO
        return null;
    }
    public Response buyMemberCart(){
        //TODO
        return null;
    }

    public Response logout(){
        //TODO
        return null;
    }
    public Response getPurchaseHistory(){
        //TODO
        return null;
    }
    public Response createStore(){
        //TODO
        return null;
    }

    public Response closeStore(){
        //TODO
        return null;
    }
    public Response reOpenStore(){
        //TODO
        return null;
    }

    public Response appointManager(){
        //TODO
        return null;
    }

    public Response appointOwner(){
        //TODO
        return null;
    }

    public Response fireManager(){
        //TODO
        return null;
    }
    public Response fireOwner(){
        //TODO
        return null;
    }
    public Response editManagerPermissions(){
        //TODO
        return null;
    }

    public Response setStoreDiscountPolicy(){
        //TODO
        return null;
    }

    public Response setPurchasePolicy(){
        //TODO
        return null;
    }
    public Response giveUpOwnerShip(){
        //TODO
        return null;
    }

    public Response getStoreHistory(){
        //TODO
        return null;
    }


}