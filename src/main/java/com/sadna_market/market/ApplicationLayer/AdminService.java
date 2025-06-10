package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.ApplicationLayer.DTOs.*;
import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.DomainServices.StoreManagementService;
import com.sadna_market.market.DomainLayer.DomainServices.UserAccessService;
import com.sadna_market.market.DomainLayer.Events.DomainEventPublisher;
import com.sadna_market.market.DomainLayer.Events.StoreClosedEvent;
import com.sadna_market.market.DomainLayer.Events.StoreReopenedEvent;
import com.sadna_market.market.DomainLayer.Events.ViolationReplyEvent;
import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final AuthenticationAdapter authentication;
    private final UserAccessService userAccessService;
    private final StoreManagementService storeManagementService;
    private final IUserRepository userRepository;
    private final IStoreRepository storeRepository;
    private final IReportRepository reportRepository;
    private final IOrderRepository orderRepository;
    private final IProductRepository productRepository;

    @Autowired
    public AdminService(AuthenticationAdapter authentication,
                        UserAccessService userAccessService,
                        StoreManagementService storeManagementService,
                        IUserRepository userRepository,
                        IStoreRepository storeRepository,
                        IReportRepository reportRepository,
                        IOrderRepository orderRepository,
                        IProductRepository productRepository) {
        this.authentication = authentication;
        this.userAccessService = userAccessService;
        this.storeManagementService = storeManagementService;
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.reportRepository = reportRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    // ==================== ADMIN VALIDATION ====================

    private void validateAdminPermissions(String adminUsername) {
        Optional<User> userOptional = userRepository.findByUsername(adminUsername);
        if (userOptional.isEmpty()) {
            logger.error("Admin user not found: {}", adminUsername);
            throw new IllegalArgumentException("Admin user not found");
        }

        User user = userOptional.get();
        if (!user.isAdmin()) {
            logger.error("User {} is not an admin", adminUsername);
            throw new IllegalArgumentException("Insufficient permissions: Admin access required");
        }

        logger.info("Admin permissions validated for user: {}", adminUsername);
    }

    // ==================== ADMIN STORE MANAGEMENT ====================

    public Response<String> closeStore(String adminUsername, String token, UUID storeId) {
        try {
            logger.info("Admin {} attempting to close store {}", adminUsername, storeId);

            // Application layer validations
            authentication.validateToken(adminUsername, token);
            validateAdminPermissions(adminUsername);

            // Verify store exists
            Optional<Store> storeOpt = storeRepository.findById(storeId);
            if (storeOpt.isEmpty()) {
                return Response.error("Store not found");
            }

            Store store = storeOpt.get();
            if (!store.isActive()) {
                return Response.error("Store is already closed");
            }

            // Close store through domain service
            storeManagementService.adminCloseStore(adminUsername, storeId);
            DomainEventPublisher.publish(new StoreClosedEvent(adminUsername, storeId));

            logger.info("Store {} closed successfully by admin {}", store.getName(), adminUsername);
            return Response.success("Store closed successfully");

        } catch (Exception e) {
            logger.error("Error closing store: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

        public Response<String> reopenStore(String adminUsername, String token, UUID storeId) {
            try {
                logger.info("Admin {} attempting to reopen store {}", adminUsername, storeId);

                // Application layer validations
                authentication.validateToken(adminUsername, token);
                validateAdminPermissions(adminUsername);

                // Verify store exists
                Optional<Store> storeOpt = storeRepository.findById(storeId);
                if (storeOpt.isEmpty()) {
                    return Response.error("Store not found");
                }

                Store store = storeOpt.get();
                if (store.isActive()) {
                    return Response.error("Store is already open");
                }

                // Use admin-specific method
                storeManagementService.adminReopenStore(adminUsername, storeId);

                // Publish the event after successful reopening
                DomainEventPublisher.publish(new StoreReopenedEvent(adminUsername,storeId));

                logger.info("Store {} reopened successfully by admin {}", store.getName(), adminUsername);
                return Response.success("Store reopened successfully");

            } catch (Exception e) {
                logger.error("Error reopening store: {}", e.getMessage());
                return Response.error(e.getMessage());
            }
        }


    // ==================== ADMIN USER MANAGEMENT ====================

    public Response<String> removeUser(String adminUsername, String token, String targetUsername) {
        try {
            logger.info("Admin {} attempting to remove user {}", adminUsername, targetUsername);

            // Application layer validations
            authentication.validateToken(adminUsername, token);
            validateAdminPermissions(adminUsername);

            // Prevent admin from removing themselves
            if (adminUsername.equals(targetUsername)) {
                return Response.error("Admin cannot remove themselves");
            }

            // Verify target user exists
            Optional<User> targetUserOpt = userRepository.findByUsername(targetUsername);
            if (targetUserOpt.isEmpty()) {
                return Response.error("User not found: " + targetUsername);
            }

            // Prevent removing other admins
            User targetUser = targetUserOpt.get();
            if (targetUser.isAdmin()) {
                return Response.error("Cannot remove other admin users");
            }

            // Remove user through domain service
            userAccessService.deleteUser(adminUsername, targetUsername);

            logger.info("User {} removed successfully by admin {}", targetUsername, adminUsername);
            return Response.success("User removed successfully");

        } catch (Exception e) {
            logger.error("Error removing user: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    // ==================== ADMIN REPORTING ====================

    public Response<List<ReportDTO>> getReports(String adminUsername, String token) {
        try {
            logger.info("Admin {} requesting violation reports", adminUsername);

            // Application layer validations
            authentication.validateToken(adminUsername, token);
            validateAdminPermissions(adminUsername);

            // Get reports through domain service
            List<Report> reports = userAccessService.getViolationReports(adminUsername);

            // Convert to DTOs
            List<ReportDTO> reportDTOs = reports.stream()
                    .map(this::convertToReportDTO)
                    .toList();

            logger.info("Retrieved {} reports for admin {}", reportDTOs.size(), adminUsername);
            return Response.success(reportDTOs);

        } catch (Exception e) {
            logger.error("Error getting reports: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response<String> respondToReport(String adminUsername, String token, UUID reportId, String responseMessage) {
        try {
            logger.info("Admin {} responding to report {}", adminUsername, reportId);

            // Application layer validations
            authentication.validateToken(adminUsername, token);
            validateAdminPermissions(adminUsername);

            // Verify report exists
            Optional<Report> reportOpt = reportRepository.findById(reportId);
            if (reportOpt.isEmpty()) {
                return Response.error("Report not found");
            }

            Report report = reportOpt.get();

            // Respond through domain service
            userAccessService.replyViolationReport(adminUsername, reportId, report.getUsername(), responseMessage);
            DomainEventPublisher.publish(new ViolationReplyEvent(
                    adminUsername, reportId, report.getUsername(), responseMessage
            ));

            logger.info("Admin {} responded to report {} successfully", adminUsername, reportId);
            return Response.success("Response sent successfully");

        } catch (Exception e) {
            logger.error("Error responding to report: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    // ==================== ADMIN SYSTEM INSIGHTS ====================

    public Response<SystemInsightsDTO> getSystemInsights(String adminUsername, String token) {
        try {
            logger.info("Admin {} requesting system insights", adminUsername);

            // Application layer validations
            authentication.validateToken(adminUsername, token);
            validateAdminPermissions(adminUsername);

            // Calculate system metrics
            int totalUsers = userRepository.countAll();
            int totalStores = storeRepository.countAll();
            int totalOrders = orderRepository.countAll();
            double totalRevenue = orderRepository.calculateTotalRevenue();
            int activeUsers = userRepository.countActiveUsers();
            int pendingReports = reportRepository.countPendingReports();

            // Get rates from domain service
            double transactionRate = userAccessService.getTransactionsRatePerHour(adminUsername);
            double subscriptionRate = userAccessService.getSubscriptionsRatePerHour(adminUsername);

            // Create insights DTO
            SystemInsightsDTO insights = new SystemInsightsDTO(
                    totalUsers, totalStores, totalOrders, totalRevenue,
                    activeUsers, pendingReports, transactionRate, subscriptionRate
            );

            logger.info("System insights retrieved for admin {}", adminUsername);
            return Response.success(insights);

        } catch (Exception e) {
            logger.error("Error getting system insights: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    public Response<List<UserDTO>> getAllUsers(String adminUsername, String token) {
        try {
            logger.info("Admin {} requesting all users", adminUsername);

            // Application layer validations
            authentication.validateToken(adminUsername, token);
            validateAdminPermissions(adminUsername);

            // Get all users
            List<User> users = userRepository.findAll();

            // Convert to DTOs
            List<UserDTO> userDTOs = users.stream()
                    .map(user -> new UserDTO(user))
                    .toList();

            logger.info("Retrieved {} users for admin {}", userDTOs.size(), adminUsername);
            return Response.success(userDTOs);

        } catch (Exception e) {
            logger.error("Error getting all users: {}", e.getMessage());
            return Response.error(e.getMessage());
        }
    }

    //Helper method to convert Report to ReportDTO
    private ReportDTO convertToReportDTO(Report report) {
        // Get store name if available
        String storeName = "Unknown Store";
        try {
            Optional<Store> storeOpt = storeRepository.findById(report.getStoreId());
            if (storeOpt.isPresent()) {
                storeName = storeOpt.get().getName();
            }
        } catch (Exception e) {
            logger.warn("Could not fetch store name for report {}", report.getReportId());
        }

        // Get product name if available
        String productName = "Unknown Product";
        try {
            Optional<Product> productOpt = productRepository.findById(report.getProductId());
            if (productOpt.isPresent()) {
                productName = productOpt.get().getName();
            }
        } catch (Exception e) {
            logger.warn("Could not fetch product name for report {}", report.getReportId());
        }

        return new ReportDTO(
                report.getReportId(),              // UUID as-is
                report.getUsername(),              // This is the reporter username
                report.getComment(),
                report.getStoreId(),               // UUID as-is
                storeName,
                report.getProductId(),             // UUID as-is
                productName,
                report.getCreatedAt()
        );
    }
}