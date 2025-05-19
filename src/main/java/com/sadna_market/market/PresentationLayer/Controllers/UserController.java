//package com.sadna_market.market.PresentationLayer.Controllers;
//import com.sadna_market.market.ApplicationLayer.DTOs.CartDTO;
//import com.sadna_market.market.ApplicationLayer.DTOs.UserDTO;
//import com.sadna_market.market.ApplicationLayer.Requests.*;
//import com.sadna_market.market.ApplicationLayer.Response;
//import com.sadna_market.market.ApplicationLayer.UserService;
//import com.sadna_market.market.DomainLayer.Report;
//import com.sadna_market.market.InfrastructureLayer.Payment.PaymentMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/users")
//public class UserController {
//
//    private final UserService userService;
//
//    public UserController(UserService userService) {
//        this.userService = userService;
//    }
//
//    ///...GUEST USERS METHODS...\\\
//
//    // Create user
//    @PostMapping
//    public ResponseEntity<Response<String>> registerUser(@RequestBody RegisterRequest user) {
//        return ResponseEntity.ok(userService.registerUser(user));
//    }
//
//    // Login
//    @PostMapping("/login")
//    public ResponseEntity<Response<String>> loginUser(@RequestParam String username, @RequestParam String password) {
//        return ResponseEntity.ok(userService.loginUser(username, password));
//    }
//
//    // Add to cart
//    @PostMapping("/cart/add")
//    public ResponseEntity<Response<CartRequest>> addToCart(
//            @RequestParam CartRequest cart,
//            @RequestParam UUID storeId,
//            @RequestParam UUID productId,
//            @RequestParam int quantity
//    ) {
//        return ResponseEntity.ok(userService.addToCart(cart,storeId, productId, quantity));
//    }
//
//    // View cart
//    @GetMapping("/cart")
//    public ResponseEntity<Response<CartRequest>> viewCart(@RequestParam CartRequest cart) {
//        return ResponseEntity.ok(userService.viewCart(cart));
//    }
//
//    // Update cart
//    @PutMapping("/cart/update")
//    public ResponseEntity<Response<CartRequest>> updateCart(
//            @RequestParam CartRequest cart,
//            @RequestParam UUID storeId,
//            @RequestParam UUID productId,
//            @RequestParam int newQuantity
//    ) {
//        return ResponseEntity.ok(userService.updateCart(cart,storeId, productId, newQuantity));
//    }
//
//    // Remove from cart
//    @DeleteMapping("/cart/remove")
//    public ResponseEntity<Response<CartRequest>> removeFromCart(
//            @RequestParam CartRequest cart,
//            @RequestParam UUID storeId,
//            @RequestParam UUID productId
//    ) {
//        return ResponseEntity.ok(userService.removeFromCart(cart,storeId, productId));
//    }
//
//    // Checkout Guest
//    @PostMapping("/checkout")
//    public ResponseEntity<Response<String>> checkout(@RequestParam CartRequest cart, @RequestParam PaymentMethod pm) {
//        return ResponseEntity.ok(userService.checkout(cart,pm));
//    }
//
//    ///...REGISTERED USERS METHODS...\\\
//
//    @PutMapping("/{username}/logout")
//    public ResponseEntity<Response<String>> logoutUser(@PathVariable String username,@RequestHeader("Authorization") String token) {
//        return ResponseEntity.ok(userService.logoutUser(username,token));
//    }
//
//    @PostMapping("/{username}/cart/add")
//    public ResponseEntity<Response<String>> addToCart(@PathVariable String username,@RequestHeader("Authorization") String token, @RequestParam UUID storeId,@RequestParam UUID productId,@RequestParam int quantity) {
//        return ResponseEntity.ok(userService.addToCart(username,token,storeId,productId,quantity));
//    }
//
//    // View cart
//    @GetMapping("/{username}/cart")
//    public ResponseEntity<Response<CartDTO>> viewCart(@PathVariable String username, @RequestHeader("Authorization") String token) {
//        return ResponseEntity.ok(userService.viewCart(username,token));
//    }
//
//    // Remove from cart
//    @DeleteMapping("/{username}/cart/remove")
//    public ResponseEntity<Response<CartDTO>> removeFromCart(
//            @PathVariable String username,
//            @RequestHeader("Authorization") String token,
//            @RequestParam UUID storeId,
//            @RequestParam UUID productId
//    ) {
//        return ResponseEntity.ok(userService.removeFromCart(username,token,storeId, productId));
//    }
//
//    // Update cart
//    @PutMapping("/cart/update")
//    public ResponseEntity<Response<CartDTO>> updateCart(
//            @PathVariable String username,
//            @RequestHeader("Authorization") String token,
//            @RequestParam UUID storeId,
//            @RequestParam UUID productId,
//            @RequestParam int newQuantity
//    ) {
//        return ResponseEntity.ok(userService.updateCart(username,token,storeId, productId, newQuantity));
//    }
//
//    // Checkout Registered
//    @PostMapping("/checkout")
//    public ResponseEntity<Response<String>> checkout( @PathVariable String username,
//                                                      @RequestHeader("Authorization") String token,
//                                                      @RequestParam PaymentMethod pm) {
//        return ResponseEntity.ok(userService.checkout(username,token,pm));
//    }
//
//    // Save product review
//    @PostMapping("/review")
//    public ResponseEntity<Response<String>> saveReview(
//            @RequestHeader("Authorization") String token,
//            @RequestParam ProductReviewRequest review
//    ) {
//        return ResponseEntity.ok(userService.saveReview(token,review));
//    }
//
//    // Save product rate
//    @PostMapping("/rate")
//    public ResponseEntity<Response<String>> saveRate(
//            @RequestHeader("Authorization") String token,
//            @RequestParam ProductRateRequest rating
//    ) {
//        return ResponseEntity.ok(userService.saveRate(token, rating));
//    }
//
//    // Report violation
//    @PostMapping("/{username}/violation")
//    public ResponseEntity<Response<String>> reportViolation(
//            @PathVariable String username,
//            @RequestHeader("Authorization") String token,
//            @RequestParam ReviewRequest report
//    ) {
//        return ResponseEntity.ok(userService.reportViolation(username, token, report));
//    }
//
//    // Get order history
//    @GetMapping("/{username}/orders")
//    public ResponseEntity<Response<List<UUID>>> getOrdersHistory(
//            @PathVariable String username,
//            @RequestHeader("Authorization") String token
//    ) {
//        return ResponseEntity.ok(userService.getOrdersHistory(username,token));
//    }
//
//    // Return product
//    @PostMapping("/{username}/return")
//    public ResponseEntity<Response<UserDTO>> returnInfo(
//            @PathVariable String username,
//            @RequestHeader("Authorization") String token
//    ) {
//        return ResponseEntity.ok(userService.returnInfo(username,token));
//    }
//
//    // Change user info
//    @PutMapping("/{username}/change-info")
//    public ResponseEntity<Response<UserDTO>> changeUserInfo(
//            @PathVariable String username,
//            @RequestHeader("Authorization") String token,
//            @RequestParam RegisterRequest user
//    ) {
//        return ResponseEntity.ok(userService.changeUserInfo(username, token, user));
//    }
//
//
//    ///...ADMIN USER METHODS...\\\
//
//    // Delete user
//    @DeleteMapping("/ban/{username}")
//    public ResponseEntity<Response<String>> deleteUser(@PathVariable String adminUsername,@RequestHeader("Authorization") String token, @RequestParam String username) {
//        return ResponseEntity.ok(userService.deleteUser(adminUsername,token,username));
//    }
//
//    // Admin: get all violation reports
//    @GetMapping("{admin}/violations")
//    public ResponseEntity<Response<List<Report>>> getViolationReports(
//            @PathVariable String admin,
//            @RequestHeader("Authorization") String token
//    ) {
//        return ResponseEntity.ok(userService.getViolationReports(admin,token));
//    }
//
//    // Admin: reply to violation report
//    @PostMapping("{admin}/violations/reply")
//    public ResponseEntity<Response<String>> replyViolationReport(
//            @PathVariable String admin,
//            @RequestHeader("Authorization") String token,
//            @RequestParam UUID reportId,
//            @RequestParam String user,
//            @RequestParam String message
//    ) {
//        return ResponseEntity.ok(userService.replyViolationReport(admin,token,reportId,user,message));
//    }
//
//    // Send message to another user
//    @PostMapping("/{admin}/message")
//    public ResponseEntity<Response<String>> sendMessageToUser(
//            @PathVariable String admin,
//            @RequestHeader("Authorization") String token,
//            @RequestParam String addressee,
//            @RequestParam String message
//    ) {
//        return ResponseEntity.ok(userService.sendMessageToUser(admin,token,addressee,message));
//    }
//
//    // Get purchased history
//    @GetMapping("/{admin}/purchase-history")
//    public ResponseEntity<Response<List<UUID>>> getUserPurchasedHistory(
//            @PathVariable String admin,
//            @RequestHeader("Authorization") String token,
//            @RequestParam String username
//    ) {
//        return ResponseEntity.ok(userService.getUserPurchasedHistory(admin,token,username));
//    }
//
//    // Get user transaction rate
//    @GetMapping("/{admin}/transaction-rate")
//    public ResponseEntity<Response<Double>> getTransactionsRate(
//            @PathVariable String admin,
//            @RequestHeader("Authorization") String token
//    ) {
//        return ResponseEntity.ok(userService.getTransactionsRate(admin,token));
//    }
//
//    // Get subscription rate
//    @GetMapping("/{admin}/subscription-rate")
//    public ResponseEntity<Response<Double>> getSubscriptionsRate(
//            @PathVariable String admin,
//            @RequestHeader("Authorization") String token
//    ) {
//        return ResponseEntity.ok(userService.getSubscriptionsRate(admin,token));
//    }
//}
