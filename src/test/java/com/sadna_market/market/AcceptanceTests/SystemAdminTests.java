package com.sadna_market.market.AcceptanceTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.*;
import com.sadna_market.market.ApplicationLayer.Requests.RegisterRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ReviewRequest;
import com.sadna_market.market.DomainLayer.Report;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")  // This activates the test profile
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
public class SystemAdminTests {
    @Autowired
    private Bridge bridge;
    ObjectMapper objectMapper = new ObjectMapper();

    // Test data
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private String adminToken;

    private String regularUsername;
    private String regularPassword;
    private String regularToken;

    private String targetUsername;
    private String targetPassword;

    private UUID reportId;

    @BeforeEach
    void setUp() {
        // Create test users
        setupUsers();

        // Create a test violation report
        setupViolationReport();
    }

    @AfterEach
    void tearDown() {
        // Clear the system state after each test
        bridge.clear();
    }

    private void setupUsers() {
        // Register admin user
        RegisterRequest adminRequest = new RegisterRequest(
                ADMIN_USERNAME,
                ADMIN_PASSWORD,
                ADMIN_USERNAME + "@example.com",
                "System",
                "Admin"
        );
        Response<String> adminRegisterResponse = bridge.registerUser(adminRequest);
        Assertions.assertFalse(adminRegisterResponse.isError(), "Admin registration should succeed");

        // Register regular user
        regularUsername = "regular";
        regularPassword = "Regular123!";
        RegisterRequest regularRequest = new RegisterRequest(
                regularUsername,
                regularPassword,
                regularUsername + "@example.com",
                "Regular",
                "User"
        );
        Response<String> regularRegisterResponse = bridge.registerUser(regularRequest);
        Assertions.assertFalse(regularRegisterResponse.isError(), "Regular user registration should succeed");

        // Register target user (to be deleted, messaged, etc.)
        targetUsername = "targetuser";
        targetPassword = "Target123!";
        RegisterRequest targetRequest = new RegisterRequest(
                targetUsername,
                targetPassword,
                targetUsername + "@example.com",
                "Target",
                "User"
        );
        Response<String> targetRegisterResponse = bridge.registerUser(targetRequest);
        Assertions.assertFalse(targetRegisterResponse.isError(), "Target user registration should succeed");

        // Login to get tokens
        Response<String> adminLoginResponse = bridge.loginUser(ADMIN_USERNAME, ADMIN_PASSWORD);
        Assertions.assertFalse(adminLoginResponse.isError(), "Admin login should succeed");
        adminToken = adminLoginResponse.getData();

        Response<String> regularLoginResponse = bridge.loginUser(regularUsername, regularPassword);
        Assertions.assertFalse(regularLoginResponse.isError(), "Regular user login should succeed");
        regularToken = regularLoginResponse.getData();
    }

    private void setupViolationReport() {
        // Create a mock UUID for store and product
        UUID mockStoreId = UUID.randomUUID();
        UUID mockProductId = UUID.randomUUID();

        // Create a violation report
        ReviewRequest reportRequest = new ReviewRequest();
        reportRequest.setStoreId(mockStoreId);
        reportRequest.setProductId(mockProductId);
        reportRequest.setComment("This product description contains inappropriate content");
        reportRequest.setViolationReport(true);
        reportRequest.setUsername(regularUsername);

        // Submit the report
        Response<String> reportResponse = bridge.reportViolation(regularUsername, regularToken, reportRequest);
        Assertions.assertFalse(reportResponse.isError(), "Report submission should succeed");

        // Get the report ID from the response or from retrieving all reports
        Response<List<Report>> reportsResponse = bridge.getViolationReports(ADMIN_USERNAME, adminToken);
        Assertions.assertFalse(reportsResponse.isError(), "Getting reports should succeed");

        if (!reportsResponse.getData().isEmpty()) {
            reportId = reportsResponse.getData().get(0).getReportId();
        } else {
            // If no reports were found, create a random UUID
            reportId = UUID.randomUUID();
        }
    }

    // POSITIVE TESTS

    @Test
        //@DisplayName("Admin should be able to delete a user")
    void adminDeleteUserTest() {
        Response<String> deleteResponse = bridge.adminDeleteUser(ADMIN_USERNAME, adminToken, targetUsername);

        Assertions.assertNotNull(deleteResponse, "Response should not be null");
        Assertions.assertFalse(deleteResponse.isError(), "Admin should be able to delete user");

        // Verify user is deleted by trying to login
        Response<String> loginResponse = bridge.loginUser(targetUsername, targetPassword);
        Assertions.assertTrue(loginResponse.isError(), "Deleted user should not be able to login");
    }

    @Test
        //@DisplayName("Admin should be able to get violation reports")
    void getViolationReportsTest() {
        Response<List<Report>> reportsResponse = bridge.getViolationReports(ADMIN_USERNAME, adminToken);

        Assertions.assertNotNull(reportsResponse, "Response should not be null");
        Assertions.assertFalse(reportsResponse.isError(), "Admin should be able to get violation reports");
        Assertions.assertNotNull(reportsResponse.getData(), "Reports data should not be null");
    }

    @Test
        //@DisplayName("Admin should be able to reply to a violation report")
    void replyToViolationReportTest() {
        String replyMessage = "Thank you for your report. We have addressed the issue.";
        Response<String> replyResponse = bridge.replyToViolationReport(
                ADMIN_USERNAME,
                adminToken,
                reportId,
                regularUsername,
                replyMessage
        );

        Assertions.assertNotNull(replyResponse, "Response should not be null");
        Assertions.assertFalse(replyResponse.isError(), "Admin should be able to reply to violation report");
    }

    @Test
        //@DisplayName("Admin should be able to send message to user")
    void adminSendMessageTest() {
        String messageContent = "This is an important system announcement.";
        Response<String> messageResponse = bridge.adminSendMessage(
                ADMIN_USERNAME,
                adminToken,
                regularUsername,
                messageContent
        );

        Assertions.assertNotNull(messageResponse, "Response should not be null");
        Assertions.assertFalse(messageResponse.isError(), "Admin should be able to send message to user");
    }

    @Test
        //@DisplayName("Admin should be able to view user purchase history")
    void getUserPurchaseHistoryTest() {
        Response<List<UUID>> historyResponse = bridge.getUserPurchaseHistory(
                ADMIN_USERNAME,
                adminToken,
                regularUsername
        );

        Assertions.assertNotNull(historyResponse, "Response should not be null");
        Assertions.assertFalse(historyResponse.isError(), "Admin should be able to view user purchase history");
        Assertions.assertNotNull(historyResponse.getData(), "Purchase history data should not be null");
    }

    @Test
        //@DisplayName("Admin should be able to get transaction rate")
    void getTransactionRateTest() {
        Response<Double> transactionRateResponse = bridge.getTransactionRate(ADMIN_USERNAME, adminToken);

        Assertions.assertNotNull(transactionRateResponse, "Response should not be null");
        Assertions.assertFalse(transactionRateResponse.isError(), "Admin should be able to get transaction rate");
        Assertions.assertNotNull(transactionRateResponse.getData(), "Transaction rate data should not be null");
    }

    @Test
        //@DisplayName("Admin should be able to get subscription rate")
    void getSubscriptionRateTest() {
        Response<Double> subscriptionRateResponse = bridge.getSubscriptionRate(ADMIN_USERNAME, adminToken);

        Assertions.assertNotNull(subscriptionRateResponse, "Response should not be null");
        Assertions.assertFalse(subscriptionRateResponse.isError(), "Admin should be able to get subscription rate");
        Assertions.assertNotNull(subscriptionRateResponse.getData(), "Subscription rate data should not be null");
    }

    // NEGATIVE TESTS

    @Test
        //@DisplayName("Non-admin user should not be able to delete another user")
    void nonAdminDeleteUserTest() {
        Response<String> deleteResponse = bridge.adminDeleteUser(regularUsername, regularToken, targetUsername);

        Assertions.assertNotNull(deleteResponse, "Response should not be null");
        Assertions.assertTrue(deleteResponse.isError(), "Non-admin user should not be able to delete another user");
        Assertions.assertNotNull(deleteResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
        //@DisplayName("Logged out admin should not be able to delete a user")
    void loggedOutAdminDeleteUserTest() {
        // First logout the admin
        Response<String> logoutResponse = bridge.logout(ADMIN_USERNAME, adminToken);
        Assertions.assertFalse(logoutResponse.isError(), "Admin logout should succeed");

        // Attempt to delete user while logged out
        Response<String> deleteResponse = bridge.adminDeleteUser(ADMIN_USERNAME, adminToken, targetUsername);

        Assertions.assertNotNull(deleteResponse, "Response should not be null");
        Assertions.assertTrue(deleteResponse.isError(), "Logged out admin should not be able to delete user");
        Assertions.assertNotNull(deleteResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
        //@DisplayName("Non-admin user should not be able to get violation reports")
    void nonAdminGetViolationReportsTest() {
        Response<List<Report>> reportsResponse = bridge.getViolationReports(regularUsername, regularToken);

        Assertions.assertNotNull(reportsResponse, "Response should not be null");
        Assertions.assertTrue(reportsResponse.isError(), "Non-admin user should not be able to get violation reports");
        Assertions.assertNotNull(reportsResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
        //@DisplayName("Non-admin user should not be able to reply to violation report")
    void nonAdminReplyToViolationTest() {
        String replyMessage = "I'll pretend to be an admin.";
        Response<String> replyResponse = bridge.replyToViolationReport(
                regularUsername,
                regularToken,
                reportId,
                regularUsername,
                replyMessage
        );

        Assertions.assertNotNull(replyResponse, "Response should not be null");
        Assertions.assertTrue(replyResponse.isError(), "Non-admin user should not be able to reply to violation report");
        Assertions.assertNotNull(replyResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
        //@DisplayName("Admin should not be able to reply to non-existent report")
    void replyToNonexistentReportTest() {
        UUID nonExistentReportId = UUID.randomUUID();
        String replyMessage = "This report doesn't exist.";

        Response<String> replyResponse = bridge.replyToViolationReport(
                ADMIN_USERNAME,
                adminToken,
                nonExistentReportId,
                regularUsername,
                replyMessage
        );

        Assertions.assertNotNull(replyResponse, "Response should not be null");
        Assertions.assertTrue(replyResponse.isError(), "Admin should not be able to reply to non-existent report");
        Assertions.assertNotNull(replyResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
        //@DisplayName("Non-admin user should not be able to send admin message")
    void nonAdminSendMessageTest() {
        String messageContent = "I'm pretending to be an admin.";
        Response<String> messageResponse = bridge.adminSendMessage(
                regularUsername,
                regularToken,
                targetUsername,
                messageContent
        );

        Assertions.assertNotNull(messageResponse, "Response should not be null");
        Assertions.assertTrue(messageResponse.isError(), "Non-admin user should not be able to send admin message");
        Assertions.assertNotNull(messageResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
        //@DisplayName("Admin should not be able to send message to non-existent user")
    void sendMessageToNonexistentUserTest() {
        String nonExistentUser = "nonexistentUser" + System.currentTimeMillis();
        String messageContent = "This user doesn't exist.";

        Response<String> messageResponse = bridge.adminSendMessage(
                ADMIN_USERNAME,
                adminToken,
                nonExistentUser,
                messageContent
        );

        Assertions.assertNotNull(messageResponse, "Response should not be null");
        Assertions.assertTrue(messageResponse.isError(), "Admin should not be able to send message to non-existent user");
        Assertions.assertNotNull(messageResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
        //@DisplayName("Non-admin user should not be able to view user purchase history")
    void nonAdminGetPurchaseHistoryTest() {
        Response<List<UUID>> historyResponse = bridge.getUserPurchaseHistory(
                regularUsername,
                regularToken,
                targetUsername
        );

        Assertions.assertNotNull(historyResponse, "Response should not be null");
        Assertions.assertTrue(historyResponse.isError(), "Non-admin user should not be able to view user purchase history");
        Assertions.assertNotNull(historyResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
        //@DisplayName("Non-admin user should not be able to get transaction rate")
    void nonAdminGetTransactionRateTest() {
        Response<Double> transactionRateResponse = bridge.getTransactionRate(regularUsername, regularToken);

        Assertions.assertNotNull(transactionRateResponse, "Response should not be null");
        Assertions.assertTrue(transactionRateResponse.isError(), "Non-admin user should not be able to get transaction rate");
        Assertions.assertNotNull(transactionRateResponse.getErrorMessage(), "Error message should not be null");
    }

    @Test
        //@DisplayName("Non-admin user should not be able to get subscription rate")
    void nonAdminGetSubscriptionRateTest() {
        Response<Double> subscriptionRateResponse = bridge.getSubscriptionRate(regularUsername, regularToken);

        Assertions.assertNotNull(subscriptionRateResponse, "Response should not be null");
        Assertions.assertTrue(subscriptionRateResponse.isError(), "Non-admin user should not be able to get subscription rate");
        Assertions.assertNotNull(subscriptionRateResponse.getErrorMessage(), "Error message should not be null");
    }
}