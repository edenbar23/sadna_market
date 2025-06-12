package com.sadna_market.market.InfrastructureLayer.Initialization;

import com.sadna_market.market.InfrastructureLayer.Initialization.SystemStateManager.InitializationMode;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class SystemConfig {
    private InitializationMode mode = InitializationMode.SELECTIVE;
    private FailureAction onFailure = FailureAction.STOP;
    private boolean rollbackOnError = true;
    private List<ComponentConfig> components = new ArrayList<>();

    public enum FailureAction {
        STOP,      // Stop on first failure
        CONTINUE,  // Continue with remaining components
        ROLLBACK   // Rollback all changes and stop
    }
}