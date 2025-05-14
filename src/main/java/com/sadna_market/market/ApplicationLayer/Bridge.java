package com.sadna_market.market.ApplicationLayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.DomainServices.*;
import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationBridge;
import com.sadna_market.market.InfrastructureLayer.Authentication.IAuthRepository;
import com.sadna_market.market.InfrastructureLayer.Authentication.InMemoryAuthRepository;
import com.sadna_market.market.InfrastructureLayer.Authentication.TokenService;
import com.sadna_market.market.InfrastructureLayer.InMemoryRepos.*;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Bridge {
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final ProductService productService;
    private final StoreService storeService;
    
    private final MessageApplicationService messageService;


    public Bridge() {
        // For backward compatibility with tests
        this.objectMapper = new ObjectMapper();

        // Create the repositories
        IRatingRepository ratingRepository = new InMemoryRatingRepository();
        IUserRepository userRepository = new InMemoryUserRepository();
        IStoreRepository storeRepository = new InMemoryStoreRepository();
        // Note: No longer passing ratingRepository to the product repository constructor
        IProductRepository productRepository = new InMemoryProductRepository();
        IOrderRepository orderRepository = new InMemoryOrderRepository();
        IMessageRepository messageRepository = new InMemoryMessageRepository();
        IReportRepository reportRepository = new InMemoryReportRepository();
        IAuthRepository authRepository = new InMemoryAuthRepository();

        TokenService tokenService = new TokenService();

        // Create authentication
        AuthenticationBridge authentication = new AuthenticationBridge(authRepository, tokenService);

        // Create domain services
        UserAccessService userAccessService = new UserAccessService(userRepository, storeRepository, reportRepository, "admin");
        StoreManagementService storeManagementService = new StoreManagementService(storeRepository, userRepository, messageRepository);
        InventoryManagementService inventoryManagementService = new InventoryManagementService(storeRepository, productRepository, userRepository);
        MessageService messageService = new MessageService(messageRepository, storeRepository, userRepository);
        RatingService ratingService = new RatingService(ratingRepository, userRepository, productRepository, storeRepository);

        // Create application services
        this.userService = new UserService(authentication, userAccessService, objectMapper);
        this.productService = new ProductService(authentication, productRepository, inventoryManagementService, ratingService, objectMapper);
        this.storeService = new StoreService(authentication, storeManagementService, storeRepository, orderRepository, ratingService, objectMapper);
        this.messageService = new MessageApplicationService(authentication, messageService, objectMapper);
    }

    /** Admin Test Methods */
    public Response deleteUser(String userName, String token, String userToDelete) {
        return userService.deleteUser(userName, token, userToDelete);
    }

    /** User Test Methods */
    public Response registerUser(RegisterRequest request) {
        return userService.registerUser(request);
    }

    public Response loginUser(String userName, String password) {
        return userService.loginUser(userName, password);
    }

    /** Store Test Methods */
    public Response addProductToStore(String token, String userName, UUID storeId, ProductRequest product, int quantity)  {
        return productService.addProduct(userName, token, product, storeId, quantity);
    }

    public Response removeProductFromStore(String token, String userName, UUID storeId, ProductRequest product) {
        return productService.deleteProduct(userName, token, product, storeId);
    }

    public Response editProductDetails(String token, String userName, UUID storeId, ProductRequest product, int quantity) {
        return productService.updateProduct(userName, token, storeId, product, quantity);
    }

    /** Guest Test Methods */
    public Response searchProduct(ProductSearchRequest request){
        return productService.searchProduct(request);
    }

    public Response addProductToGuestCart(CartRequest cart, UUID storeId, UUID productId, int quantity){
        return userService.addToCart(cart, storeId, productId, quantity);
    }

    public Response removeProductFromGuestCart(CartRequest cart, UUID storeId, UUID productId){
        return userService.removeFromCart(cart, storeId, productId);
    }

    public Response viewGuestCart(CartRequest cart){
        return userService.viewCart(cart);
    }

    public Response updateGuestCart(CartRequest cart, UUID storeId, UUID productId, int quantity){
        return userService.updateCart(cart, storeId, productId, quantity);
    }

    public Response buyGuestCart(CartRequest cart, PaymentMethod paymentMethod){
        return userService.checkout(cart, paymentMethod);
    }

    /** Registered User Methods */
    public Response addProductToUserCart(String userName, String token, UUID storeId, UUID productId, int quantity){
        return userService.addToCart(userName, token, storeId, productId, quantity);
    }

    public Response removeProductFromUserCart(String userName, String token, UUID storeId, UUID productId){
        return userService.removeFromCart(userName, token, storeId, productId);
    }

    public Response viewUserCart(String userName, String token){
        return userService.viewCart(userName, token);
    }

    public Response updateUserCart(String userName, String token, UUID storeId, UUID productId, int quantity){
        return userService.updateCart(userName, token, storeId, productId, quantity);
    }

    public Response buyUserCart(String userName, String token, PaymentMethod paymentMethod){
        return userService.checkout(userName, token, paymentMethod);
    }

    public Response rateProduct(String token, ProductRateRequest request){
        return productService.rateProduct(token, request);
    }

    public Response getOrdersHistory(String userName, String token){
        return userService.getOrdersHistory(userName, token);
    }

    public Response sendMessage(String username, String token, MessageRequest request){
        return messageService.sendMessage(username, token, request);
    }

    public Response replyToMessage(String username, String token, MessageReplyRequest request){
        return messageService.replyToMessage(username, token, request);
    }

    public Response getUserMessages(String username, String token){
        return messageService.getUserMessages(username, token);
    }

    public Response deleteMessage(String username, String token, UUID messageId){
        return messageService.deleteMessage(username, token, messageId);
    }

    public Response logout(String userName, String token){
        return userService.logoutUser(userName, token);
    }

    public Response getPurchaseHistory(String username, String token, UUID storeId){
        //return storeService.getPurchaseHistory(username, token, storeId);
        return Response.error("not implemented");
    }

    public Response createStore(String username, String token, StoreRequest newStore){
        return storeService.openStore(username, token, newStore);
    }

    public Response closeStore(String username, String token, UUID storeId){
        return storeService.closeStore(username, token, storeId);
    }

    public Response reopenStore(String username, String token, UUID storeId){
        return storeService.reopenStore(username, token, storeId);
    }

    public Response appointManager(String username, String token, UUID storeId, String manager, PermissionsRequest permissions){
        return storeService.appointStoreManager(username, token, storeId, manager, permissions);
    }

    public Response appointOwner(String username, String token, UUID storeId, String newOwner){
        return storeService.appointStoreOwner(username, token, storeId, newOwner);
    }

    public Response removeManager(String username, String token, UUID storeId, String manager){
        return storeService.removeStoreManager(username, token, storeId, manager);
    }

    public Response removeOwner(String username, String token, UUID storeId, String manager){
        return storeService.removeStoreOwner(username, token, storeId, manager);
    }

    public Response editManagerPermissions(String username, String token, UUID storeId, String manager, PermissionsRequest permissions){
        return storeService.changePermissions(username, token, storeId, manager, permissions);
    }

    public Response giveUpOwnerShip(String username, String token, UUID storeId){
        return storeService.leaveOwnership(username, token, storeId);
    }

    public void clear() {
        userService.clear();
        productService.clear();
        // user service clears also the store repo via UserAccessService
        messageService.clear();
    }
}