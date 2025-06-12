package com.sadna_market.market.InfrastructureLayer.Initialization;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InitializationResult {
    private SystemStateManager.ComponentStatus overallStatus;
    private String errorMessage;
    private List<ComponentResult> componentResults = new ArrayList<>();

    public void addComponentResult(ComponentResult result) {
        componentResults.add(result);
    }
}
