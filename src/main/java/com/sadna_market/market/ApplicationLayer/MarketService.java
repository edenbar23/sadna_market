package com.sadna_market.market.ApplicationLayer;

import java.util.UUID;

import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.DomainLayer.IProductRepository;
import com.sadna_market.market.DomainLayer.IStoreRepository;
import com.sadna_market.market.DomainLayer.IUserRepository;
import com.sadna_market.market.DomainLayer.Product.ProductDTO;
import com.sadna_market.market.InfrastructureLayer.RepositoryConfiguration;
import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationBridge;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;

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

    MessageApplicationService messageService;
    //IPaymentGateway service
    //supplySystem+
    private RepositoryConfiguration RC;
    //Constructor
    public MarketService() {
        this.RC = new RepositoryConfiguration();
        IUserRepository userRepository=RC.userRepository();
        IProductRepository productRepository=  RC.productRepository();
        IStoreRepository storeRepository= RC.storeRepository();
        this.userService = UserService.getInstance(RC);
        this.productService = ProductService.getInstance(productRepository);
        this.storeService = StoreService.getInstance(RC);
        this.messageService = MessageApplicationService.getInstance(RC);
    }






    //II) Users functions here:
    //Guest functions here:
    //req 1.3
    public Response registerUser(RegisterRequest registerRequest) {
        //register a new user
        Response json = userService.registerUser(registerRequest);
        if(!json.isError()) {
            authentication.saveUser(registerRequest.getUserName(),registerRequest.getPassword());
        }
        return json;
    }
    //req 1.4
    public Response login(String userName, String password) {
        //login to the system
        userService.loginUser(userName,password);
        //auth return token
        String token = authentication.createUserSessionToken(userName,password);
        return Response.success(token);
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

    //req 2.2
    public Response searchProduct(ProductSearchRequest search) {
        //search for a product
        return productService.searchProduct(search);
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
    public Response updateCart(CartRequest cart,UUID storeId, UUID productId, int quantity) {
        //update a product from the cart
        return userService.updateCart(cart,storeId,productId,quantity);
    }
    //req 2.4 (c)
    public Response removeFromCart(CartRequest cart, UUID storeId, UUID productId) {
        //remove a product from the cart
        return userService.removeFromCart(cart,storeId,productId);
    }
    //req 2.5
    public Response checkout(CartRequest cart, PaymentMethod paymentMethod) {
        //checkout the cart
        return userService.checkout(cart,paymentMethod);
    }



    //Registered User functions here:
    //req 3.1
    public Response logout(String username, String token) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.logoutUser(username);
    }
    //req 2.1 - 2.5 for registered users
    public Response addToCart(String username,String token,UUID storeId, UUID productId, int quantity) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.addToCart(username,storeId,productId,quantity);
    }
    public Response removeFromCart(String username,String token,UUID storeId, UUID productId) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.removeFromCart(username,storeId,productId);
    }
    public Response updateCart(String username,String token, UUID storeId, UUID productId, int quantity) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.updateCart(username,storeId,productId,quantity);
    }
    public Response viewCart(String username,String token) {
        //view the cart
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.viewCart(username);
    }
    public Response checkout(String username,String token, PaymentMethod paymentMethod) {
        //checkout the cart
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
       return userService.checkout(username,paymentMethod);
    }
    //

    //req 3.2
    public Response openStore(String username,String token,StoreRequest newStore) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.openStore(username,newStore);
    }

    //req 3.3
    public Response reviewProduct(String username, String token, ProductReviewRequest review) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        productService.addProductReview(review);
        // need to change implementation in UserService
        return userService.saveReview(review);
    }
    //req 3.4 (a)
    //TO-DO FIX THE PRODUCT RATE REQUEST
    public Response rateProduct(String username, String token, ProductRateRequest rate) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        Response output = productService.rateProduct(rate);
        return userService.saveRate(rate);
        //return output;
    }
    //req 3.4 (b)
    public Response rateStore(String username, String token, StoreRateRequest rate) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
//        productService.addRate(rate);
//        userService.saveRate(rate);
        return Response.error("not implemented yet");
    }
    //req 3.5
    public Response sendMessageToStore(String username, String token, UUID storeId, String message) {
        try {
            authenticate(username, token);
        } catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }

        MessageRequest request = new MessageRequest(storeId, message);
        return messageService.sendMessage(username, request);
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
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.getOrdersHistory(username);
    }
    //req 3.8 (a)
    public Response getUserInfo(String username, String token) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        //return a Response<UserDTO> in future
        return userService.returnInfo(username);
    }
    //req 3.8 (b)
    public Response changeUserInfo(String username, String token, RegisterRequest user) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.changeUserInfo(username,user);
    }

    //StoreOwner functions here:
    //req 4.1 (a)
    public Response addProductToStore(String username, String token, UUID storeId, ProductRequest product, int quantity) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return Response.error("not implemented yet");
    }

    //req 4.1 (b)
    public Response removeProductFromStore(String username,String token, UUID storeId,ProductRequest product) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return Response.error("not implemented yet");
    }
    //req 4.1 (c)
    public Response updateProductOfStore(String username,String token,UUID storeId,ProductRequest product, int quantity) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
//        if(userService.canUpdateProductToStore(username,storeId)) {
//            return productService.updateProduct(product);
//            //should return a Response with productId
//        }
        return Response.error("not implemented yet");
    }
    //req 4.2 (a)
    public Response updateStoreDiscountPolicy(String username,String token,UUID storeId,DiscountPolicyRequest discount) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        //check if the user has permission to update the store discount policy
//        if(userService.canUpdateStoreDiscountPolicy(username,storeId)) {
//            storeService.updateStoreDiscountPolicy(storeId, discount);
//            //should return a Response with productId
//        }
//        else {
//            throw new IllegalArgumentException("User does not have permission to update store discount policy");
//        }
        return Response.error("not implemented yet");

    }
    //req 4.2 (b)
    public Response changeStorePurchasePolicy(String username,String token,UUID storeId,ProductDiscountPolicyRequest policy) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        //check if the user has permission to update the store purchase policy
//        if(userService.canUpdateStorePurchasePolicy(username,storeId)) {
//            storeService.updateStorePurchasePolicy(storeId, policy);
//            //should return a Response with productId
//        }
//        else {
//            throw new IllegalArgumentException("User does not have permission to update store purchase policy");
//        }
        return Response.error("not implemented yet");
    }
    //req 4.2 (c)
    public Response changeProductDiscountPolicy(String username, String token, UUID productId, ProductDiscountPolicyRequest discount) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        //check if the user has permission to update the store purchase policy
//        if(userService.canUpdateProductDiscountPolicy(username,productId)) {
//            productService.updateProductDiscountPolicy(productId, discount);
//            //should return a Response with productId
//        }
//        else {
//            throw new IllegalArgumentException("User does not have permission to update store purchase policy");
//        }
        return Response.error("not implemented yet");
    }
    //req 4.2 (d)
    public Response changeProductPurchasePolicy(String username, String token, UUID productId, ProductPurchasePolicyRequest policy) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
//        if(userService.canUpdateProductPurchasePolicy(username,productId)) {
//            productService.updateProductPurchasePolicy(productId, policy);
//            //should return a Response with productId
//        }
//        else {
//            throw new IllegalArgumentException("User does not have permission to update store purchase policy");
//        }
        return Response.error("not implemented yet");
    }
    //req 4.3
    public Response appointStoreOwner(String username,String token,UUID storeId, String newOwner) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.appointStoreOwner(username,storeId,newOwner);
    }
    //req 4.4
    public Response removeStoreOwner(String username,String token,UUID storeId,String toRemove) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.removeStoreOwner(username,storeId,toRemove);
    }
    //req 4.5
    public Response leaveOwnership(String username,String token,UUID storeId) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.leaveOwnership(username,storeId);
    }
    //req 4.6
    public Response appointStoreManager(String username,String token,UUID storeId, String manager, PermissionsRequest permissions) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.appointStoreManager(username,storeId,manager,permissions);
    }
    //req 4.7
    public Response changePermissions(String username,String token,UUID storeId, String manager,PermissionsRequest permissions) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.changePermissions(username,storeId,manager,permissions);
    }
    //req 4.8
    public Response removeStoreManager(String username,String token,UUID storeId, String manager) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.removeStoreManager(username,storeId,manager);
    }
    //req 4.9 + req 6.1
    public Response closeStore(String username,String token,UUID storeId) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.closeStore(username,storeId);
    }
    //req 4.10
    public Response reopenStore(String username,String token,UUID storeId) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.reopenStore(username,storeId);
    }
    //req 4.11
    public Response getStoreRolesInfo(String username,String token,UUID storeId) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.getStoreInfo(storeId);
    }
    //req 4.12 (a)
    public Response viewStoreMessages(String username,String token,UUID storeId) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.viewStoreMessages(username,storeId);
    }
    //req 4.12 (b)
    public Response replyToMessage(String username, String token, UUID messageId, String replyContent) {
        try {
            authenticate(username, token);
        } catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }

        MessageReplyRequest request = new MessageReplyRequest(messageId, replyContent);
        return messageService.replyToMessage(username, request);
    }
    //req 4.13
    public Response getPurchaseHistory(String username,String token,UUID storeId) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.getPurchaseHistory(username,storeId);
    }

    //StoreManager functions here:
    //req 5
    public Response getStoreManagerPermissions(String username,String token,UUID storeId) {
        try{
            authenticate(username,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.getStoreManagerPermissions(username,storeId);
    }
    //Store functions here:
    //

    //Product functions here:
    //

    //SystemAdmin functions here:
    //req 6.2
    public Response removeUser(String admin,String token,String userToDelete) {
        try{
            authenticate(admin,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.deleteUser(admin,userToDelete);
    }
    //req 6.3 (a)
    public Response getViolationReports(String admin,String token) {
        try{
            authenticate(admin,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.getViolationReports(admin);
    }
    //req 6.3 (b)
    public Response replyViolationReport(String admin,String token,UUID reportId,String user,String message) {
        try{
            authenticate(admin,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.replyViolationReport(admin,reportId,user,message);
    }
    //req 6.3 (c)
    public Response sendMessageToUser(String admin,String token,String addresse,String message) {
        try{
            authenticate(admin,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.sendMessageToUser(admin,addresse,message);
    }
    //req 6.4 (a)
    public Response getUserPurchaseHistory(String admin,String username,String token) {
        try{
            authenticate(admin,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.getUserPurchasedHistory(admin,username);
    }
    //req 6.4 (b)
    public Response getStorePurchaseHistory(String admin,String token,UUID storeId) {
        try{
            authenticate(admin,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.getStorePurchaseHistory(admin,storeId);
    }
    //req 6.5 (a)
    public Response getBuyersRate(String admin,String token) {
        try{
            authenticate(admin,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return storeService.getBuyersRate(admin);
    }
    //req 6.5 (b)
    public Response getTransactionsRate(String admin,String token) {
        try{
            authenticate(admin,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.getTransactionsRate(admin);
    }
    //req 6.5 (c)
    public Response getSubscriptionsRate(String admin,String token) {
        try{
            authenticate(admin,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        return userService.getSubscriptionsRate(admin);
    }

    //System functions here:
    public Response openMarket(String admin,String token) {
        //open the market
        //initialize the system
        //validate admin user exists
        //initialize the supply system
        //initialize the payment system
        //open market
        return Response.error("not implemented yet");
    }

    public Response closeMarket(String admin,String token) {
        try{
            authenticate(admin,token);
        }
        catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }
        //close the market (not allowing anyone to access market)
        return Response.error("not implemented yet");
    }

    public Response getStoreMessages(String username, String token, UUID storeId) {
        try {
            authenticate(username, token);
        } catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }

        return messageService.getStoreMessages(username, storeId);
    }

    public Response getUnansweredStoreMessages(String username, String token, UUID storeId) {
        try {
            authenticate(username, token);
        } catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }

        return messageService.getUnansweredStoreMessages(username, storeId);
    }

    public Response getUnreadStoreMessages(String username, String token, UUID storeId) {
        try {
            authenticate(username, token);
        } catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }

        return messageService.getUnreadStoreMessages(username, storeId);
    }

    public Response getUserMessages(String username, String token) {
        try {
            authenticate(username, token);
        } catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }

        return messageService.getUserMessages(username);
    }

    public Response getUserStoreConversation(String username, String token, UUID storeId) {
        try {
            authenticate(username, token);
        } catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }

        return messageService.getUserStoreConversation(username, storeId);
    }

    public Response deleteMessage(String username, String token, UUID messageId) {
        try {
            authenticate(username, token);
        } catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }

        return messageService.deleteMessage(username, messageId);
    }

    public Response markMessageAsRead(String username, String token, UUID messageId) {
        try {
            authenticate(username, token);
        } catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }

        return messageService.markMessageAsRead(username, messageId);
    }

    public Response markAllStoreMessagesAsRead(String username, String token, UUID storeId) {
        try {
            authenticate(username, token);
        } catch (IllegalArgumentException e) {
            return Response.error(e.getMessage());
        }

        return messageService.markAllStoreMessagesAsRead(username, storeId);
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
