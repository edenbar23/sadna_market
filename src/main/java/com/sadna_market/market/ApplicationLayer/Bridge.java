package com.sadna_market.market.ApplicationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;


public class Bridge {
    private MarketService service;
    private ObjectMapper objectMapper = new ObjectMapper();
    /** Admin Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * Admin component. These methods test the functionality of admin operations including
     * user management, store management, and system-wide policies.
     */

    public Response deleteUser( String userName, String token, String userToDelete) {
        //return service.deleteUser(userName, token, userToDelete);
        return null;
    }

    /**User Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * User component. These methods test the functionality of user operations including
     * user registration, login, and profile management.
     */
    public Response registerUser(RegisterRequest request) {
        //return service.registerUser(request);
        return null;
    }
    public Response loginUser(String userName, String password) {
        //return service.loginUser(userName, password);
        return null;
    }

    /**
     * Store Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * Store component. These methods test the functionality of stores including inventory
     * management, product listings, pricing strategies, and store policies.
     */
    public Response addProductToStore(String token, String userName, String storeId, ProductRequest product)  {
        //return service.addProductToStore(token, userId, storeId, product);
        return null;
    }
    public Response removeProductFromStore(String token, String userName, String storeId, String productId) {
        //return service.removeProductFromStore(token,userName,storeId,productId);
        return null;
    }
    public Response editProductDetails(String token, String userName, String storeId, ProductRequest product) {
        //return service.updateProductOfStore(token, userName, storeId, product);
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

    public Response searchProduct(ProductSearchRequest request){
        //return service.searchProduct(request);
        return null;
    }
    public Response addProductToGuestCart(CartRequest cart, String productId, int quantity){
        //return service.addToCart(cart, productId, quantity);
        return null;
    }

    public Response removeProductFromGuestCart(CartRequest cart, String productId){
        //return service.removeFromCart(cart, productId);
        return null;
    }
    public Response viewGuestCart(CartRequest cart){
        //return service.viewCart(cart);
        return null;
    }
    public Response updateGuestCart(CartRequest cart, String productId, int quantity){
        //return service.updateCart(cart, productId, quantity);
        return null;
    }


    public Response buyGuestCart(CartRequest cart){
        //return service.checkout(cart);
        return null;
    }

    /**
     * User Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * User component. These methods test the functionality for registered users including
     * login/logout, profile management, order history, and user-specific permissions.
     */
    public Response addProductToUserCart(String userName,String token, UUID productId, int quantity){
        //return service.addToCart(userName, token, productId, quantity);
        return null;
    }

    public Response removeProductFromUserCart(String userName,String token, UUID productId){
        //return service.removeFromCart(userName, token, productId);
        return null;
    }

    public Response viewUserCart(String userName,String token){
        //return service.viewCart(userName);
        return null;
    }
    public Response updateUserCart(String userName,String token, UUID productId, int quantity){
        //return service.updateCart(userName, token, productId, quantity);
        return null;
    }
    public Response buyUserCart(String userName,String token){
        //return service.checkout(userName, token);
        return null;
    }

    public Response logout(String userName, String token){
        //return service.logout(userName, token);
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