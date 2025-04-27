package com.sadna_market.market.ApplicationLayer;

import java.util.UUID;

import com.sadna_market.market.DomainLayer.IProductRepository;
import com.sadna_market.market.DomainLayer.IStoreRepository;
import com.sadna_market.market.DomainLayer.IUserRepository;
import com.sadna_market.market.DomainLayer.Product.ProductDTO;
import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationBridge;

//this is going to be the API for the market
public class MarketService {
//this MarketService is a proxy for the market's services with the authentication stage
    //Auth auth
    AuthenticationBridge authentication = new AuthenticationBridge();
    //IUserRepository userRepository
//    IUserRepository userRepository;
    UserService userService;
//    IProductRepository productRepository;
    ProductService productService;
//    IStoreRepository storeRepository;
    StoreService storeService;
    //IPaymentGateway service
    //supplySystem+

    //Constructor
    public MarketService(IUserRepository userRepository, IProductRepository productRepository, IStoreRepository storeRepository) {
//        this.userRepository = userRepository;
//        this.productRepository = productRepository;
//        this.storeRepository = storeRepository;
        this.userService = new UserService(userRepository);
//        this.productService = new ProductService(productRepository);
        this.storeService = new StoreService(storeRepository, userRepository);
    }
    //System functions here:
    public void openMarket() {
        //open the market
        //initialize the system
        //validate admin user exists
        //initialize the supply system
        //initialize the payment system
        //open market
    }

    //Admin functions here:
    //
    public Response deleteUser(String username,String token, String userToDelete) {
        //authenticate the user
        //authenticateAdmin(username,token);
        //if not, throw an exception
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        //delete user from the system
        return userService.deleteUser(username,token,userToDelete);
    }


    //II) Users functions here:
    //Guest functions here:
    //req 1.3
    public Response registerUser(RegisterRequest registerRequest) {
        //register a new user
        return userService.registerUser(registerRequest);
    }
    //req 1.4
    public void login(String userName, String password) {
        //login to the system
        return userService.loginUser(userName,password);
        //auth return token
    }
    //req 2.1 (a)
    public ProductDTO getProductInfo(UUID productId) {
        //get product info
        return productService.getProductInfo(productId);
    }
    //req 2.1 (b)
    public Response getStoreInfo(UUID storeId) {
        //get store info
        return storeService.getStoreInfo(storeId);
    }

    //req 2.2 (a)
    public Response searchProduct(ProductSearchRequest search) {
        //search for a product
        return productService.searchProduct(search);
    }
    //req 2.2 (b)
    public Response searchStore(SearchRequest search) {
        //search for a store
        return storeService.searchStore(search);
    }

    //req 2.3
    public Response addToCart(CartRequest cart, UUID productId, UUID storeId, int quantity) {
        //add a product to the cart
        return userService.addToCart(cart,storeId,productId,quantity);
    }
    //req 2.4 (a)
    public Response viewCart(CartRequest cart) {
        //view the cart
        return userService.viewCart(cart);
    }
    //req 2.4 (b)
    public Response updateCart(CartRequest cart, UUID productId, int quantity) {
        //update a product from the cart
        return userService.updateCart(cartId,productId,quantity);
    }
    //req 2.4
    public Response removeFromCart(CartRequest cart, UUID productId) {
        //remove a product from the cart
        return userService.removeFromCart(productId);
    }
    //req 2.5
    public Response checkout(CartRequest cart) {
        //checkout the cart
        return userService.checkout(cart);
    }



    //Registered User functions here:
    //req 3.1
    public Response logout(String userName, String token) {
        //logout from the system
        authenticate(userName,token);
        //String token = auth.logout(userName);
        return userService.logoutUser(userName);
    }
    //req 2.1 - 2.5 for registered users
    public Response addToCart(String userName,String token, UUID productId, int quantity) {
        //check if the token is valid
        authenticate(userName,token);
        //if not, throws an exception
        return userService.addToCart(userName,productId,quantity);
    }
    public Response removeFromCart(String userName,String token, UUID productId) {
        //remove a product from the cart
        //check if the token is valid
        authenticate(userName,token);
        //if not, throw an exception
        return userService.removeFromCart(userName,productId);
    }
    public Response updateCart(String userName,String token, UUID productId, int quantity) {
        //update a product in the cart
        //check if the token is valid
        authenticate(userName,token);
        //if not, throw an exception
        return userService.updateCart(userName,productId,quantity);
    }
    public Response viewCart(String userName,String token) {
        //view the cart
        //check if the token is valid
        authenticate(userName,token);
        //if not, throw an exception
        return userService.viewCart(userName);
    }
    public Response checkout(String userName,String token) {
        //checkout the cart
        //check if the token is valid
        authenticate(userName,token);
        //if not, throw an exception
       return userService.checkout(userName);
    }
    //

    //req 3.2
    public Response openStore(String username,String token,StoreRequest newStore) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return storeService.openStore(newStore);
    }

    //req 3.3
    public Response reviewProduct(String username, String token, ProductReviewRequest review) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        productService.addProductReview(review);
        // need to change implementation in UserService
        return userService.saveReview(review);
    }
    //req 3.4 (a)
    public Response rateProduct(String username, String token, ProductRateRequest rate) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        Response output = productService.rateProduct(rate);
        return userService.saveRate(rate);
        return output;
    }
    //req 3.4 (b)
    public void rateStore(String username, String token, RateRequest rate) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        productService.addRate(rate);
        userService.saveRate(rate);
    }
    //req 3.5
    public void sendMessageToStore(String username, String token,UUID storeId, String message) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        userService.sendMessage(username,storeId,message);
        storeService.receiveMessage(storeId,username,message);
    }
    //req 3.6
    public Response reportViolation(String username, String token,ReviewRequest report) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return userService.reportViolation(username,report);
    }
    //req 3.7
    public Response getOrdersHistory(String username, String token) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return userService.getOrdersHistory(username);
    }
    //req 3.8 (a)
    public Response getUserInfo(String username, String token) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        //return a Response<UserDTO> in future
        return userService.returnInfo(username);
    }
    //req 3.8 (b)
    public Response changeUserInfo(String username, String token, RegisterRequest user) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return userService.changeUserInfo(username,user);
    }

    //StoreOwner functions here:
    //req 4.1 (a)
    public Response addProductToStore(String username,String token,UUID storeId,ProductRequest product) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        if(userService.canAddProductToStore(username,storeId)) {
            return productService.addProduct(product, storeId);
            //should return a Response with productId
        }
        else {
            throw new IllegalArgumentException("User does not have permission to add product to store");
        }
    }
    //req 4.1 (b)
    public Response removeProductFromStore(String username,String token, UUID storeId,ProductRequest product) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        if(userService.canRemoveProductToStore(username,storeId)) {
            // not good enough - need to decide exactly what to do here
            return productService.deleteProduct(product);
            //should return a Response with productId
        }
        else {
            throw new IllegalArgumentException("User does not have permission to remove product from store");
        }
    }
    //req 4.1 (c)
    public Response updateProductOfStore(String username,String token,UUID storeId,ProductRequest product) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        if(userService.canUpdateProductToStore(username,storeId)) {
            return productService.updateProduct(product);
            //should return a Response with productId
        }
        else {
            throw new IllegalArgumentException("User does not have permission to remove product from store");
        }
    }
    //req 4.2 (a)
    public Response updateStoreDiscountPolicy(String username,String token,UUID storeId,DiscountPolicyRequest discount) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        //check if the user has permission to update the store discount policy
//        if(userService.canUpdateStoreDiscountPolicy(username,storeId)) {
//            storeService.updateStoreDiscountPolicy(storeId, discount);
//            //should return a Response with productId
//        }
//        else {
//            throw new IllegalArgumentException("User does not have permission to update store discount policy");
//        }
        return null;

    }
    //req 4.2 (b)
    public Response changeStorePurchasePolicy(String username,String token,UUID storeId,PurchasePolicyRequest policy) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        //check if the user has permission to update the store purchase policy
//        if(userService.canUpdateStorePurchasePolicy(username,storeId)) {
//            storeService.updateStorePurchasePolicy(storeId, policy);
//            //should return a Response with productId
//        }
//        else {
//            throw new IllegalArgumentException("User does not have permission to update store purchase policy");
//        }
        return null;
    }
    //req 4.2 (c)
    public Response changeProductDiscountPolicy(String username,String token,UUID productId,ProductDiscountPolicyRequest discount) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        //check if the user has permission to update the store purchase policy
//        if(userService.canUpdateProductDiscountPolicy(username,productId)) {
//            productService.updateProductDiscountPolicy(productId, discount);
//            //should return a Response with productId
//        }
//        else {
//            throw new IllegalArgumentException("User does not have permission to update store purchase policy");
//        }
        return null;
    }
    //req 4.2 (d)
    public Response changeProductPurchasePolicy(String username,String token,UUID productId,ProductPurchasePolicyRequest policy) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
//        if(userService.canUpdateProductPurchasePolicy(username,productId)) {
//            productService.updateProductPurchasePolicy(productId, policy);
//            //should return a Response with productId
//        }
//        else {
//            throw new IllegalArgumentException("User does not have permission to update store purchase policy");
//        }
        return null;
    }
    //req 4.3
    public Response appointStoreOwner(String username,String token,UUID storeId, String newOwner) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return userService.appointStoreOwner(username,storeId,newOwner);
    }
    //req 4.4
    public Response removeStoreOwner(String username,String token,UUID storeId) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return userService.removeStoreOwner(username,storeId);
    }
    //req 4.5
    public Response leaveOwnership(String username,String token,UUID storeId) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return userService.leaveOwnership(username,storeId);
    }
    //req 4.6
    public Response appointStoreManager(String username,String token,UUID storeId, String manager, PermissionsRequest permissions) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return storeService.appointStoreManager(username,storeId,manager,permissions);
    }
    //req 4.7
    public Response changePermissions(String username,String token,UUID storeId, String manager,PermissionsRequest permissions) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return userService.changePermissions(username,storeId,manager,permissions);
    }
    //req 4.8
    public Response removeStoreManager(String username,String token,UUID storeId, String manager) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return userService.removeStoreManager(username,storeId,manager);
    }
    //req 4.9 + req 6.1
    public Response closeStore(String username,String token,UUID storeId) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return storeService.closeStore(username,storeId);
    }
    //req 4.10
    public Response reopenStore(String username,String token,UUID storeId) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return storeService.reopenStore(username,storeId);
    }
    //req 4.11
    public Response getStoreRolesInfo(String username,String token,UUID storeId) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return storeService.getStoreInfo(storeId);
    }
    //req 4.12 (a)
    public Response viewStoreMessages(String username,String token,UUID storeId) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return userService.viewStoreMessages(username,storeId);
    }
    //req 4.12 (b)
    public Response replyToStoreMessage(String username,String token,UUID storeId,UUID messageId,String targetUser, String message) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return userService.replyToStoreMessage(username,storeId,messageId,targetUser,message);
    }
    //req 4.13
    public Response getPurchaseHistory(String username,String token,UUID storeId) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return userService.getPurchaseHistory(username,storeId);
    }

    //StoreManager functions here:
    //req 5
    public Response getStoreManagerPermissions(String username,String token,UUID storeId) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        return userService.getStoreManagerPermissions(username,storeId);
    }
    //Store functions here:
    //

    //Product functions here:
    //

    //SystemAdmin functions here:
    //req 6.2
    public Response removeUser(String admin,String token,String userToDelete) {
        //check if the token is valid
        authenticate(admin,token);
        //if not, throw an exception
        return userService.deleteUser(admin,userToDelete);
    }
    //req 6.3 (a)
    public Response getViolationReports(String admin,String token) {
        //check if the token is valid
        authenticate(admin,token);
        //if not, throw an exception
        return userService.getViolationReports(admin);
    }
    //req 6.3 (b)
    public Response replyViolationReport(String admin,String token,UUID reportId,String message) {
        //check if the token is valid
        authenticate(admin,token);
        //if not, throw an exception
        return userService.replyViolationReport(admin,reportId,message);
    }
    //req 6.3 (c)
    public Response sendMessageToUser(String admin,String token,String addresse,String message) {
        //check if the token is valid
        authenticate(admin,token);
        //if not, throw an exception
        return userService.sendMessageToUser(admin,addresse,message);
    }
    //req 6.4 (a)
    public Response getUserPurchaseHistory(String admin,String username,String token) {
        //check if the token is valid
        authenticate(admin,token);
        //if not, throw an exception
        return userService.getUserPurchasedHistory(admin,username);
    }
    //req 6.4 (b)
    public Response getStorePurchaseHistory(String admin,String token,UUID storeId) {
        //check if the token is valid
        authenticate(admin,token);
        //if not, throw an exception
        return userService.getUserPurchaseHistory(admin,storeId);
    }
    //req 6.5 (a)
    public Response getBuyersRate(String admin,String token) {
        //check if the token is valid
        authenticate(admin,token);
        //if not, throw an exception
        return userService.getBuyersRate(admin);
    }
    //req 6.5 (b)
    public Response getTransactionsRate(String admin,String token) {
        //check if the token is valid
        authenticate(admin,token);
        //if not, throw an exception
        return userService.getTransactionsRate(admin);
    }
    //req 6.5 (c)
    public Response getSubscriptionsRate(String admin,String token) {
        //check if the token is valid
        authenticate(admin,token);
        //if not, throw an exception
        return userService.getSubscriptionsRate(admin);
    }

    public void closeMarket(String admin,String token) {
        //check if the token is valid
        authenticate(admin,token);
        //if not, throw an exception
        //close the market (not allowing anyone to access market)
    }



    //helper functions here:
    protected void authenticate(String username, String token) {
        //authenticate the user
        //if not, throw an exception
        if(!authentication.checkSessionToken(authentication.checkSessionToken(token)).equals(username)) {
            throw new IllegalArgumentException("Invalid token");
        }
    }
}
