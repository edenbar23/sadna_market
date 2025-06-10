package com.sadna_market.market.PresentationLayer.Controllers;

import com.sadna_market.market.ApplicationLayer.AdminService;
import com.sadna_market.market.ApplicationLayer.DTOs.ReportDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.SystemInsightsDTO;
import com.sadna_market.market.ApplicationLayer.DTOs.UserDTO;
import com.sadna_market.market.ApplicationLayer.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for handling admin-related operations
 * Provides endpoints for system administration functions
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    //---------------------------
    // Store Management Endpoints
    //---------------------------

    /**
     * Admin: Close a store
     */
    @PostMapping("/{adminUsername}/stores/{storeId}/close")
    public ResponseEntity<Response<String>> closeStore(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token,
            @PathVariable UUID storeId) {

        Response<String> response = adminService.closeStore(adminUsername, token, storeId);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }


    //---------------------------
    // User Management Endpoints
    //---------------------------

    /**
     * Admin: Remove a user from the system
     */
    @DeleteMapping("/{adminUsername}/users/{targetUsername}")
    public ResponseEntity<Response<String>> removeUser(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token,
            @PathVariable String targetUsername) {

        Response<String> response = adminService.removeUser(adminUsername, token, targetUsername);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Admin: Get all users in the system
     */
    @GetMapping("/{adminUsername}/users")
    public ResponseEntity<Response<List<UserDTO>>> getAllUsers(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token) {

        Response<List<UserDTO>> response = adminService.getAllUsers(adminUsername, token);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    //---------------------------
    // Reports Management Endpoints
    //---------------------------

    /**
     * Admin: Get all violation reports
     */
    @GetMapping("/{adminUsername}/reports")
    public ResponseEntity<Response<List<ReportDTO>>> getReports(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token) {

        Response<List<ReportDTO>> response = adminService.getReports(adminUsername, token);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Admin: Respond to a violation report
     */
    @PostMapping("/{adminUsername}/reports/{reportId}/respond")
    public ResponseEntity<Response<String>> respondToReport(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token,
            @PathVariable UUID reportId,
            @RequestBody String responseMessage) {

        Response<String> response = adminService.respondToReport(adminUsername, token, reportId, responseMessage);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //---------------------------
    // System Insights Endpoints
    //---------------------------

    /**
     * Admin: Get system insights and statistics
     */
    @GetMapping("/{adminUsername}/insights")
    public ResponseEntity<Response<SystemInsightsDTO>> getSystemInsights(
            @PathVariable String adminUsername,
            @RequestHeader("Authorization") String token) {

        Response<SystemInsightsDTO> response = adminService.getSystemInsights(adminUsername, token);

        return response.isError()
                ? ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
                : ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for admin service
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Admin service is running");
    }
}