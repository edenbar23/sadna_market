package com.sadna_market.market.ApplicationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.DomainLayer.IProductRepository;
import com.sadna_market.market.DomainLayer.IStoreRepository;
import com.sadna_market.market.DomainLayer.IUserRepository;
import com.sadna_market.market.InfrastructureLayer.*;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;

import java.util.UUID;


public class Bridge {
    private ObjectMapper objectMapper = new ObjectMapper();

    private RepositoryConfiguration RC;
    UserService userService;
    ProductService productService;
    StoreService storeService;
    MessageApplicationService messageService;

    /** Admin Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * Admin component. These methods test the functionality of admin operations including
     * user management, store management, and system-wide policies.
     */

    public Bridge(){

        this.RC = new RepositoryConfiguration();
        IUserRepository userRepository=RC.userRepository();
        IProductRepository productRepository=  RC.productRepository();
        IStoreRepository storeRepository= RC.storeRepository();
        this.userService = UserService.getInstance(RC);
        this.productService = ProductService.getInstance(productRepository);
        this.storeService = StoreService.getInstance(RC);
        this.messageService = MessageApplicationService.getInstance(RC);
    }
    public Response deleteUser(String userName, String token, String userToDelete) {
        return userService.deleteUser(userName, token, userToDelete);
    }

    /**User Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * User component. These methods test the functionality of user operations including
     * user registration, login, and profile management.
     */
    public Response registerUser(RegisterRequest request) {
        return userService.registerUser(request);
    }
    public Response loginUser(String userName, String password) {
        return userService.loginUser(userName, password);
    }

    /**
     * Store Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * Store component. These methods test the functionality of stores including inventory
     * management, product listings, pricing strategies, and store policies.
     */
    public Response addProductToStore(String token, String userName, UUID storeId, ProductRequest product,int quantity)  {
        //return service.addProductToStore(token, userName, storeId, product, quantity);
        //TODO: implement this
        return Response.error("not implemented");
    }
    public Response removeProductFromStore(String token, String userName, UUID storeId,  ProductRequest product) {
        //TODO: implement this
        return Response.error("not implemented");
    }
    public Response editProductDetails(String token, String userName, UUID storeId, ProductRequest product, int quantity) {
        //TODO: implement this
        return Response.error("not implemented");
    }


    /**
     * Guest Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * Guest component. These methods test the functionality available to non-registered
     * users including browsing products, adding items to carts, and registering for
     * an account.
     */


    public Response searchProduct(ProductSearchRequest request){
        return productService.searchProduct(request);
    }
    public Response addProductToGuestCart(CartRequest cart, UUID storeId, UUID productId, int quantity){
        return userService.addToCart(cart,storeId,productId,quantity);
    }

    public Response removeProductFromGuestCart(CartRequest cart,UUID storeId, UUID productId){
        return userService.removeFromCart(cart,storeId,productId);
    }
    public Response viewGuestCart(CartRequest cart){
        return userService.viewCart(cart);
    }

    public Response updateGuestCart(CartRequest cart,UUID storeId, UUID productId, int quantity){
        return userService.updateCart(cart,storeId,productId,quantity);
    }


    public Response buyGuestCart(CartRequest cart, PaymentMethod paymentMethod){
        return userService.checkout(cart,paymentMethod);
    }

    /**
     * User Test Methods
     *
     * This section contains methods that support black-box acceptance testing for the
     * User component. These methods test the functionality for registered users including
     * login/logout, profile management, order history, and user-specific permissions.
     */
    public Response addProductToUserCart(String userName,String token,UUID storeId, UUID productId, int quantity){
        return userService.addToCart(userName,token,storeId,productId,quantity);
    }

    public Response removeProductFromUserCart(String userName,String token,UUID storeId, UUID productId){
        return userService.removeFromCart(userName, token, storeId, productId);
    }

    public Response viewUserCart(String userName,String token){
        return userService.viewCart(userName, token);
    }
    public Response updateUserCart(String userName,String token,UUID storeId, UUID productId, int quantity){
        return userService.updateCart(userName,token, storeId,productId,quantity);
    }
    public Response buyUserCart(String userName,String token,PaymentMethod paymentMethod){
        return userService.checkout(userName, token, paymentMethod);
    }

    public Response logout(String userName, String token){
        return userService.logoutUser(userName, token);
    }
    public Response getPurchaseHistory(String username,String token,UUID storeId){
        //return storeService.getPurchaseHistory(username, token, storeId);
        return Response.error("not implemented");
    }
    public Response createStore(String username, String token, StoreRequest newStore){
        return storeService.openStore(username, token, newStore);
    }

    public Response closeStore(String username,String token,UUID storeId){
        return storeService.closeStore(username, token, storeId);
    }
    public Response reopenStore(String username,String token,UUID storeId){
        return storeService.reopenStore(username, token, storeId);
    }

    public Response appointManager(String username,String token,UUID storeId, String manager, PermissionsRequest permissions){
        return storeService.appointStoreManager(username, token, storeId, manager, permissions);
    }

    public Response appointOwner(String username,String token,UUID storeId, String newOwner){
        return storeService.appointStoreOwner(username, token, storeId, newOwner);
    }

    public Response removeManager(String username,String token,UUID storeId, String manager){
        return storeService.removeStoreManager(username, token, storeId, manager);
    }
    public Response removeOwner(String username,String token,UUID storeId, String manager){
        return storeService.removeStoreOwner(username, token, storeId, manager);
    }
    public Response editManagerPermissions(String username,String token,UUID storeId, String manager,PermissionsRequest permissions){
        return storeService.changePermissions(username, token, storeId, manager, permissions);
    }

    public Response giveUpOwnerShip(String username,String token,UUID storeId){
        return storeService.leaveOwnership(username, token, storeId);
    }

}