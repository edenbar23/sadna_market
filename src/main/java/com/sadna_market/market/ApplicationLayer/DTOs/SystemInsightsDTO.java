package com.sadna_market.market.ApplicationLayer.DTOs;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class SystemInsightsDTO {

    private int totalUsers;
    private int totalStores;
    private int totalOrders;
    private double totalRevenue;
    private int activeUsers;
    private int pendingReports;
    private double transactionRate;
    private double subscriptionRate;
    private LocalDateTime lastUpdated;

    public SystemInsightsDTO(int totalUsers, int totalStores, int totalOrders,
                             double totalRevenue, int activeUsers, int pendingReports,
                             double transactionRate, double subscriptionRate) {
        this.totalUsers = totalUsers;
        this.totalStores = totalStores;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.activeUsers = activeUsers;
        this.pendingReports = pendingReports;
        this.transactionRate = transactionRate;
        this.subscriptionRate = subscriptionRate;
        this.lastUpdated = LocalDateTime.now();
    }

}