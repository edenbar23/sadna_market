package com.sadna_market.market.InfrastructureLayer.Initialization;

import lombok.Data;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InitializationStateRepository {

    private final Map<String, ComponentStateRecord> stateRecords = new ConcurrentHashMap<>();

    public void saveComponentState(String componentId,
                                   SystemStateManager.ComponentStatus status,
                                   Map<String, String> createdEntities,
                                   String errorMessage) {
        ComponentStateRecord record = new ComponentStateRecord();
        record.componentId = componentId;
        record.status = status;
        record.createdEntities = createdEntities;
        record.errorMessage = errorMessage;
        record.timestamp = System.currentTimeMillis();

        stateRecords.put(componentId, record);
    }

    public ComponentStateRecord getComponentState(String componentId) {
        return stateRecords.get(componentId);
    }

    public void clearAll() {
        stateRecords.clear();
    }

    @Data
    public static class ComponentStateRecord {
        String componentId;
        SystemStateManager.ComponentStatus status;
        Map<String, String> createdEntities;
        String errorMessage;
        long timestamp;
    }
}