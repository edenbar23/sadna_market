package com.sadna_market.market.InfrastructureLayer.Initialization;

import lombok.Data;

@Data
 public class ComponentResult {
    private String componentId;
    private SystemStateManager.ComponentStatus status;
    private String errorMessage;
    private java.util.Map<String, String> createdEntities = new java.util.HashMap<>();
    private java.util.Map<String, String> rollbackData = new java.util.HashMap<>();
    private java.util.Map<String, Object> details = new java.util.HashMap<>();

    public ComponentResult(String componentId) {
        this.componentId = componentId;
    }

    public void addCreatedEntity(String type, String id) {
        createdEntities.put(type, id);
    }

    public void addRollbackAction(String action, String target) {
        rollbackData.put(action, target);
    }
}
