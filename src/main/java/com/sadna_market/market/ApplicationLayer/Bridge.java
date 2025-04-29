package com.sadna_market.market.ApplicationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.InfrastructureLayer.*;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;

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

    public Bridge(){
        this.service = new MarketService();
    }
    public Response deleteUser( String userName, String token, String userToDelete) {
        return service.removeUser(userName, token, userToDelete);

    }

    /**User Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * User component. These methods test the functionality of user operations including
     * user registration, login, and profile management.
     */
    public Response registerUser(RegisterRequest request) {
        return service.registerUser(request);
    }
    public Response loginUser(String userName, String password) {
        return service.login(userName, password);
    }

    /**
     * Store Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * Store component. These methods test the functionality of stores including inventory
     * management, product listings, pricing strategies, and store policies.
     */
    public Response addProductToStore(String token, String userName, UUID storeId, ProductRequest product,int quantity)  {
        return service.addProductToStore(token, userName, storeId, product, quantity);
    }
    public Response removeProductFromStore(String token, String userName, UUID storeId,  ProductRequest product) {
        return service.removeProductFromStore(token,userName,storeId,product);
    }
    public Response editProductDetails(String token, String userName, UUID storeId, ProductRequest product, int quantity) {
        return service.updateProductOfStore(token, userName, storeId, product, quantity);
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
        return service.searchProduct(request);
    }
    public Response addProductToGuestCart(CartRequest cart, UUID storeId, UUID productId, int quantity){
        return service.addToCart(cart, storeId, productId, quantity);
    }

    public Response removeProductFromGuestCart(CartRequest cart,UUID storeId, UUID productId){
        return service.removeFromCart(cart, storeId, productId);
    }
    public Response viewGuestCart(CartRequest cart){
        return service.viewCart(cart);
    }

    public Response updateGuestCart(CartRequest cart,UUID storeId, UUID productId, int quantity){
        return service.updateCart(cart, storeId, productId, quantity);
    }


    public Response buyGuestCart(CartRequest cart, PaymentMethod paymentMethod){
        return service.checkout(cart, paymentMethod);
    }

    /**
     * User Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * User component. These methods test the functionality for registered users including
     * login/logout, profile management, order history, and user-specific permissions.
     */
    public Response addProductToUserCart(String userName,String token,UUID storeId, UUID productId, int quantity){
        return service.addToCart(userName, token,storeId, productId, quantity);
    }

    public Response removeProductFromUserCart(String userName,String token,UUID storeId, UUID productId){
        return service.removeFromCart(userName, token,storeId, productId);
    }

    public Response viewUserCart(String userName,String token){
        return service.viewCart(userName,token);
    }
    public Response updateUserCart(String userName,String token,UUID storeId, UUID productId, int quantity){
        return service.updateCart(userName, token, storeId, productId, quantity);
    }
    public Response buyUserCart(String userName,String token,PaymentMethod paymentMethod){
        return service.checkout(userName, token, paymentMethod);
    }

    public Response logout(String userName, String token){
        return service.logout(userName, token);
    }
    public Response getPurchaseHistory(String username,String token,UUID storeId){
        return service.getPurchaseHistory(username, token, storeId);
    }
    public Response createStore(String username, String token, StoreRequest newStore){
        return service.openStore(username, token, newStore);
    }

    public Response closeStore(String username,String token,UUID storeId){
        return service.closeStore(username, token, storeId);
    }
    public Response reopenStore(String username,String token,UUID storeId){
        return service.reopenStore(username, token, storeId);
    }

    public Response appointManager(String username,String token,UUID storeId, String manager, PermissionsRequest permissions){
        return service.appointStoreManager(username,token,storeId,manager,permissions);
    }

    public Response appointOwner(String username,String token,UUID storeId, String newOwner){
        return service.appointStoreOwner(username,token,storeId,newOwner);
    }

    public Response removeManager(String username,String token,UUID storeId, String manager){
        return service.removeStoreManager(username,token,storeId,manager);
    }
    public Response removeOwner(String username,String token,UUID storeId, String manager){
        return service.removeStoreOwner(username,token,storeId,manager);
    }
    public Response editManagerPermissions(String username,String token,UUID storeId, String manager,PermissionsRequest permissions){
        return service.changePermissions(username,token,storeId,manager,permissions);
    }

    public Response setStoreDiscountPolicy(){
        //TODO
        return null;
    }

    public Response setPurchasePolicy(){
        //TODO
        return null;
    }
    public Response giveUpOwnerShip(String username,String token,UUID storeId){
        return service.leaveOwnership(username, token, storeId);
    }

}