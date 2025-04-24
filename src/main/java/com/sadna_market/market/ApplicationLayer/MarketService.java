package com.sadna_market.market.ApplicationLayer;

import java.util.UUID;

import com.sadna_market.market.DomainLayer.IProductRepository;
import com.sadna_market.market.DomainLayer.IStoreRepository;
import com.sadna_market.market.DomainLayer.IUserRepository;
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
        this.storeService = new StoreService(storeRepository);
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
    public void deleteUser(String username,String token, String userToDelete) {
        //authenticate the user
        String realToken = auth.authenticate(username);
        //if not, throw an exception

        //delete user from the system
        userService.deleteUser(username,token,userToDelete);
    }


    //II) Users functions here:
    //Guest functions here:
    //req 1.3
    public void registerUser(RegisterRequest registerRequest) {
        //register a new user
        userService.registerUser(registerRequest);
    }
    //req 1.4
    public void login(String userName, String password) {
        //login to the system
        userService.loginUser(userName,password);
        //auth return token
    }
    //req 2.1 (a)
    public void getProductInfo(String productId) {
        //get product info
        //return productService.getProductInfo(productId);
    }
    //req 2.1 (b)
    public void getStoreInfo(String storeId) {
        //get store info
        return storeService.getStoreInfo(storeId);
    }

    //req 2.2 (a)
    public void searchProduct(SearchRequest search) {
        //search for a product
        //productService.searchProduct(search);
    }
    //req 2.2 (b)
    public void searchStore(SearchRequest search) {
        //search for a store
        storeService.searchStore(search);
    }
    //req 2.3
    public void addToCart(CartRequest cart, String productId, int quantity) {
        //add a product to the cart
        userService.addToCart(cart,productId,quantity);
    }
    //req 2.4 (a)
    public void viewCart(CartRequest cart) {
        //view the cart
        userService.viewCart(cart);
    }
    //req 2.4 (b)
    public void updateCart(CartRequest cart, String productId, int quantity) {
        //remove a product from the cart
        //userService.removeFromCart(cartId,productId,quantity);
    }
    //req 2.4
    public void removeFromCart(CartRequest cart, String productId) {
        //remove a product from the cart
        //userService.removeFromCart(productId);
    }
    //req 2.5
    public void checkout(CartRequest cart) {
        //checkout the cart
        //userService.checkout(cart);
    }



    //Registered User functions here:
    //req 3.1
    public void logout(String userName, String token) {
        //logout from the system
        authenticate(userName,token);
        //String token = auth.logout(userName);
        userService.logoutUser(userName);
    }
    //req 2.1 - 2.5 for registered users
    public void addToCart(String userName,String token, String productId, int quantity) {
        //check if the token is valid
        authenticate(userName,token);
        //if not, throws an exception
        userService.addToCart(userName,productId,quantity);
    }
    public void removeFromCart(String userName,String token, String productId) {
        //remove a product from the cart
        //check if the token is valid
        authenticate(userName,token);
        //if not, throw an exception
        userService.removeFromCart(userName,productId);
    }
    public void updateCart(String userName,String token, String productId, int quantity) {
        //update a product in the cart
        //check if the token is valid
        authenticate(userName,token);
        //if not, throw an exception
        userService.updateCart(userName,productId,quantity);
    }
    public void viewCart(String userName,String token) {
        //view the cart
        //check if the token is valid
        authenticate(userName,token);
        //if not, throw an exception
        userService.viewCart(userName);
    }
    public void checkout(String userName,String token) {
        //checkout the cart
        //check if the token is valid
        authenticate(userName,token);
        //if not, throw an exception
        userService.checkout(userName);
    }
    //

    //req 3.2
    public void openStore(String username,String token,StoreRequest newStore) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        storeService.openStore(newStore);
    }

    //req 3.3
    public void reviewProduct(String username, String token, ReviewRequest review) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        productService.addReview(review);
        userService.saveReview(review);
    }
    //req 3.4 (a)
    public void rateProduct(String username, String token, RateRequest rate) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        productService.addRate(rate);
        userService.saveRate(rate);
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
    public void reportViolation(String username, String token,ReviewRequest report) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        userService.reportViolation(username,report);
    }
    //req 3.7
    public void getOrdersHistory(String username, String token) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        userService.getOrdersHistory(username);
    }
    //req 3.8 (a)
    public void getUserInfo(String username, String token) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        //return a Response<UserDTO> in future
        userService.returnInfo(username);
    }
    //req 3.8 (b)
    public void changeUserInfo(String username, String token, RegisterRequest user) {
        //check if the token is valid
        authenticate(username,token);
        //if not, throw an exception
        userService.changeUserInfo(username,user);
    }

    //StoreOwner functions here:
    //

    //Store functions here:
    //

    //Product functions here:
    //


    //helper functions here:
    protected void authenticate(String username, String token) {
        //authenticate the user
        //if not, throw an exception
        if(!authentication.checkSessionToken(authentication.checkSessionToken(token)).equals(username)) {
            throw new IllegalArgumentException("Invalid token");
        }
    }
}
