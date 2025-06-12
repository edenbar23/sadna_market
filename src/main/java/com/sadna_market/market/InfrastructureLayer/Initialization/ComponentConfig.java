package com.sadna_market.market.InfrastructureLayer.Initialization;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
class ComponentConfig {
    private String id;
    private boolean enabled = true;
    private boolean force = false;  // Force execution even if already completed
    private List<String> dependsOn = new ArrayList<>();
    private java.util.Map<String, Object> config = new java.util.HashMap<>();
}
