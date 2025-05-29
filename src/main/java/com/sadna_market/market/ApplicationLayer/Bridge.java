package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.ApplicationLayer.DTOs.*;
import com.sadna_market.market.ApplicationLayer.Requests.*;
import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class Bridge {
    private final UserService userService;
    private final ProductService productService;
    private final StoreService storeService;

    private final MessageApplicationService messageService;

//    public Bridge() {
//        // Create the repositories
//        IRatingRepository ratingRepository = new InMemoryRatingRepository();
//        IUserRepository userRepository = new InMemoryUserRepository();
//        IStoreRepository storeRepository = new InMemoryStoreRepository();
//        IProductRepository productRepository = new InMemoryProductRepository();
//        IOrderRepository orderRepository = new InMemoryOrderRepository();
//        IMessageRepository messageRepository = new InMemoryMessageRepository();
//        IReportRepository reportRepository = new InMemoryReportRepository();
//        IAuthRepository authRepository = new InMemoryAuthRepository();
//
//        TokenService tokenService = new TokenService();
//
//        // Create authentication
//        AuthenticationBridge authentication = new AuthenticationBridge(authRepository, tokenService);
//
//        // Create domain services
//        UserAccessService userAccessService = new UserAccessService(userRepository, storeRepository, reportRepository, "admin");
//        OrderProcessingService orderProcessingService = new OrderProcessingService(storeRepository, orderRepository, userRepository, productRepository);
//        StoreManagementService storeManagementService = new StoreManagementService(storeRepository, userRepository, messageRepository);
//        InventoryManagementService inventoryManagementService = new InventoryManagementService(storeRepository, productRepository, userRepository);
//        MessageService messageService = new MessageService(messageRepository, storeRepository, userRepository);
//        RatingService ratingService = new RatingService(ratingRepository, userRepository, productRepository, storeRepository,orderRepository);
//
//        // Create application services
//        this.userService = new UserService(authentication, userAccessService, inventoryManagementService, orderProcessingService);
//        this.productService = new ProductService(authentication, productRepository, inventoryManagementService, ratingService);
//        this.storeService = new StoreService(authentication, storeManagementService, storeRepository, orderRepository, ratingService);
//        this.messageService = new MessageApplicationService(authentication, messageService);
//    }

    @Autowired
    public Bridge(
            // Inject Application Services
            UserService userService,
            ProductService productService,
            StoreService storeService,
            MessageApplicationService messageService) {

        this.userService = userService;
        this.productService = productService;
        this.storeService = storeService;
        this.messageService = messageService;
    }

    /** Admin Test Methods */
    public Response<String> deleteUser(String userName, String token, String userToDelete) {
        return userService.deleteUser(userName, token, userToDelete);
    }

    // top rated stores and products
    public Response<List<Store>> getTopRatedStores() {
        return storeService.getTopRatedStores();
    }
    public Response<List<ProductDTO>> getTopRatedProducts(UUID storeId) {
        return productService.getTopRatedProductsByStore(storeId);
    }
    /** User Test Methods */
    public Response<String> registerUser(RegisterRequest request) {
        return userService.registerUser(request);
    }

    public Response<String> loginUser(String userName, String password) {
        return userService.loginUser(userName, password);
    }

    /** Store Test Methods */
    public Response<String> addProductToStore(String token, String userName, UUID storeId, ProductRequest product, int quantity)  {
        return productService.addProduct(userName, token, product, storeId, quantity);
    }

    public Response<String> removeProductFromStore(String token, String userName, UUID storeId, ProductRequest product) {
        return productService.deleteProduct(userName, token, product, storeId);
    }

    public Response<String> editProductDetails(String token, String userName, UUID storeId, ProductRequest product, int quantity) {
        return productService.updateProduct(userName, token, storeId, product, quantity);
    }

    /** Guest Test Methods */
    public Response<List<ProductDTO>> searchProduct(ProductSearchRequest request){
        return productService.searchProduct(request);
    }

    public Response<CartRequest> addProductToGuestCart(CartRequest cart, UUID storeId, UUID productId, int quantity){
        return userService.addToCart(cart, storeId, productId, quantity);
    }

    public Response<CartRequest> removeProductFromGuestCart(CartRequest cart, UUID storeId, UUID productId){
        return userService.removeFromCart(cart, storeId, productId);
    }

    public Response<CartRequest> viewGuestCart(CartRequest cart){
        return userService.viewCart(cart);
    }

    public Response<CartRequest> updateGuestCart(CartRequest cart, UUID storeId, UUID productId, int quantity){
        return userService.updateCart(cart, storeId, productId, quantity);
    }

//    public Response<String> buyGuestCart(CartRequest cart, PaymentMethod paymentMethod){
//        return userService.checkout(cart, paymentMethod);
//    }

    /** Registered User Methods */
    public Response<String> addProductToUserCart(String userName, String token, UUID storeId, UUID productId, int quantity){
        return userService.addToCart(userName, token, storeId, productId, quantity);
    }

    public Response<CartDTO> removeProductFromUserCart(String userName, String token, UUID storeId, UUID productId){
        return userService.removeFromCart(userName, token, storeId, productId);
    }

    public Response<CartDTO> viewUserCart(String userName, String token){
        return userService.viewCart(userName, token);
    }

    public Response<CartDTO> updateUserCart(String userName, String token, UUID storeId, UUID productId, int quantity){
        return userService.updateCart(userName, token, storeId, productId, quantity);
    }

//    public Response<String> buyUserCart(String userName, String token, PaymentMethod paymentMethod){
//        return userService.checkout(userName, token, paymentMethod);
//    }

    public Response<ProductRatingDTO> rateProduct(String token, ProductRateRequest request){
        return productService.rateProduct(token, request);
    }

    public Response<ProductReviewDTO> reviewProduct(String username, String token, ProductReviewRequest request){
        return productService.reviewProduct(username, token, request);
    }

    public Response<String> reportViolation(String username, String token, ReviewRequest report){
        return userService.reportViolation(username, token, report);
    }

    public Response<List<UUID>> getOrdersHistory(String userName, String token){
        return userService.getOrdersHistory(userName, token);
    }

    public Response<MessageDTO> sendMessage(String username, String token, MessageRequest request){
        return messageService.sendMessage(username, token, request);
    }

    public Response<String> replyToMessage(String username, String token, MessageReplyRequest request){
        return messageService.replyToMessage(username, token, request);
    }

    public Response<List<MessageDTO>> getUserMessages(String username, String token){
        return messageService.getUserMessages(username, token);
    }

    public Response<String> deleteMessage(String username, String token, UUID messageId){
        return messageService.deleteMessage(username, token, messageId);
    }

    public Response<String> logout(String userName, String token){
        return userService.logoutUser(userName, token);
    }

    public Response<String> getPurchaseHistory(String username, String token, UUID storeId){
        //return storeService.getPurchaseHistory(username, token, storeId);
        return Response.error("not implemented");
    }

    public Response<StoreDTO> createStore(String username, String token, StoreRequest newStore){
        return storeService.createStore(username, token, newStore);
    }

    public Response<String> closeStore(String username, String token, UUID storeId){
        return storeService.closeStore(username, token, storeId);
    }

    public Response<String> reopenStore(String username, String token, UUID storeId){
        return storeService.reopenStore(username, token, storeId);
    }

    public Response<String> appointManager(String username, String token, UUID storeId, String manager, PermissionsRequest permissions){
        return storeService.appointStoreManager(username, token, storeId, manager, permissions);
    }

    public Response<String> appointOwner(String username, String token, UUID storeId, String newOwner){
        return storeService.appointStoreOwner(username, token, storeId, newOwner);
    }

    public Response<String> removeManager(String username, String token, UUID storeId, String manager){
        return storeService.removeStoreManager(username, token, storeId, manager);
    }

    public Response<String> removeOwner(String username, String token, UUID storeId, String manager){
        return storeService.removeStoreOwner(username, token, storeId, manager);
    }

    public Response<String> editManagerPermissions(String username, String token, UUID storeId, String manager, PermissionsRequest permissions){
        return storeService.changePermissions(username, token, storeId, manager, permissions);
    }

    public Response<String> giveUpOwnerShip(String username, String token, UUID storeId){
        return storeService.leaveOwnership(username, token, storeId);
    }

    /** Admin Operations */

// Deletes a user from the system (req 6.2)
    public Response<String> adminDeleteUser(String adminUsername, String token, String userToDelete) {
        return userService.deleteUser(adminUsername, token, userToDelete);
    }


    // Gets all violation reports (req 6.3.a)
    public Response<List<Report>> getViolationReports(String adminUsername, String token) {
        return userService.getViolationReports(adminUsername, token);
    }

    // Allows admin to reply to a violation report (req 6.3.b)
    public Response<String> replyToViolationReport(String adminUsername, String token, UUID reportId,
                                                   String user, String message) {
        return userService.replyViolationReport(adminUsername, token, reportId, user, message);
    }

    // Allows admin to send a direct message to a user (req 6.3.c)
    public Response<String> adminSendMessage(String adminUsername, String token, String recipient,
                                             String message) {
        return userService.sendMessageToUser(adminUsername, token, recipient, message);
    }

    // Gets purchase history for a specific user (req 6.4.a)
    public Response<List<UUID>> getUserPurchaseHistory(String adminUsername, String token,
                                                       String targetUsername) {
        return userService.getUserPurchasedHistory(adminUsername, token, targetUsername);
    }

    // Gets the transaction rate statistics (req 6.5.b)
    public Response<Double> getTransactionRate(String adminUsername, String token) {
        return userService.getTransactionsRate(adminUsername, token);
    }

    // Gets the subscription rate statistics (req 6.5.c)
    public Response<Double> getSubscriptionRate(String adminUsername, String token) {
        return userService.getSubscriptionsRate(adminUsername, token);
    }


    public void clear() {
        userService.clear();
        productService.clear();
        // user service clears also the store repo via UserAccessService
        messageService.clear();
    }
}