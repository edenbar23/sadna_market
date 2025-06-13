// ===================================================================
// StateBasedSystemInitializer.java - CommandLineRunner Replacement
// ===================================================================

package com.sadna_market.market.InfrastructureLayer;

import com.sadna_market.market.InfrastructureLayer.Initialization.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * State-Based System Initializer - Version 4.0
 *
 * Replaces the old SystemInitializer with advanced features:
 * - Configurable initialization modes
 * - State tracking and resume capability
 * - Rollback on failure
 * - Selective component execution
 * - Interactive mode
 * - Proper failure handling
 */
@Component
@Profile("!test")
@ConditionalOnProperty(
        name = "system.startup.menu.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class StateBasedSystemInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(StateBasedSystemInitializer.class);

    @Autowired
    private SystemStateManager stateManager;

    @Autowired
    private SystemConfigLoader configLoader;

    @Value("${system.init.mode:selective}")
    private String initMode;

    @Value("${system.init.interactive:false}")
    private boolean interactiveMode;

    @Override
    public void run(String... args) {
        logger.info("=== STATE-BASED SYSTEM INITIALIZATION v4.0 ===");
        logger.info("Enhanced initialization with state management and rollback capabilities");

        try {
            SystemConfig config = configLoader.loadConfig();

            // Override mode from properties if set
            if (!initMode.equals("selective")) {
                config.setMode(SystemStateManager.InitializationMode.valueOf(initMode.toUpperCase()));
            }

            // Handle interactive mode
            if (interactiveMode) {
                config = handleInteractiveMode(config);
            }

            // Execute initialization
            InitializationResult result = stateManager.executeInitialization(config);

            // Report results
            reportResults(result);

        } catch (Exception e) {
            logger.error("=== SYSTEM INITIALIZATION FAILED ===");
            logger.error("Error: {}", e.getMessage(), e);
            throw new RuntimeException("System initialization failed", e);
        }
    }

    private SystemConfig handleInteractiveMode(SystemConfig config) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n=== INTERACTIVE SYSTEM INITIALIZATION ===");
        System.out.println("Current configuration loaded. What would you like to do?");
        System.out.println("[1] Check current state only");
        System.out.println("[2] Initialize missing components (selective)");
        System.out.println("[3] Force full initialization");
        System.out.println("[4] Reset everything and reinitialize");
        System.out.println("[5] Use current configuration as-is");
        System.out.print("Choose option (1-5): ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                config.setMode(SystemStateManager.InitializationMode.CHECK_ONLY);
                System.out.println("✓ Mode set to: Check current state only");
                break;
            case "2":
                config.setMode(SystemStateManager.InitializationMode.SELECTIVE);
                System.out.println("✓ Mode set to: Initialize missing components");
                break;
            case "3":
                config.setMode(SystemStateManager.InitializationMode.FORCE_FULL);
                System.out.println("✓ Mode set to: Force full initialization");
                break;
            case "4":
                config.setMode(SystemStateManager.InitializationMode.RESET_AND_INIT);
                System.out.print("⚠️  This will DELETE ALL DATA. Type 'CONFIRM' to proceed: ");
                String confirmation = scanner.nextLine().trim();
                if ("CONFIRM".equals(confirmation)) {
                    System.out.println("✓ Mode set to: Reset and reinitialize");
                } else {
                    System.out.println("❌ Reset cancelled, falling back to selective mode");
                    config.setMode(SystemStateManager.InitializationMode.SELECTIVE);
                }
                break;
            case "5":
                System.out.println("✓ Using current configuration");
                break;
            default:
                System.out.println("❌ Invalid choice, using selective mode");
                config.setMode(SystemStateManager.InitializationMode.SELECTIVE);
        }

        // Ask about failure handling
        System.out.print("\nWhat should happen if a component fails? [stop/continue/rollback] (default: stop): ");
        String failureChoice = scanner.nextLine().trim().toLowerCase();

        switch (failureChoice) {
            case "continue":
                config.setOnFailure(SystemConfig.FailureAction.CONTINUE);
                config.setRollbackOnError(false);
                System.out.println("✓ Will continue on failure");
                break;
            case "rollback":
                config.setOnFailure(SystemConfig.FailureAction.ROLLBACK);
                config.setRollbackOnError(true);
                System.out.println("✓ Will rollback on failure");
                break;
            default:
                config.setOnFailure(SystemConfig.FailureAction.STOP);
                System.out.println("✓ Will stop on failure");
        }

        System.out.println("\n=== Configuration Complete ===");
        return config;
    }

    private void reportResults(InitializationResult result) {
        logger.info("=== INITIALIZATION RESULTS ===");
        logger.info("Overall Status: {}", result.getOverallStatus());

        if (result.getErrorMessage() != null) {
            logger.error("Error: {}", result.getErrorMessage());
        }

        // Component-by-component results
        for (ComponentResult componentResult : result.getComponentResults()) {
            String status = componentResult.getStatus().toString();
            String componentId = componentResult.getComponentId();

            switch (componentResult.getStatus()) {
                case COMPLETED:
                    logger.info("✓ {}: {}", componentId, status);
                    logComponentDetails(componentResult);
                    break;
                case SKIPPED:
                    logger.info("⏭ {}: {} (already completed or disabled)", componentId, status);
                    break;
                case FAILED:
                    logger.error("❌ {}: {} - {}", componentId, status, componentResult.getErrorMessage());
                    break;
                default:
                    logger.info("ℹ {}: {}", componentId, status);
            }
        }

        // Summary
        long completed = result.getComponentResults().stream()
                .mapToLong(r -> r.getStatus() == SystemStateManager.ComponentStatus.COMPLETED ? 1 : 0)
                .sum();
        long failed = result.getComponentResults().stream()
                .mapToLong(r -> r.getStatus() == SystemStateManager.ComponentStatus.FAILED ? 1 : 0)
                .sum();
        long skipped = result.getComponentResults().stream()
                .mapToLong(r -> r.getStatus() == SystemStateManager.ComponentStatus.SKIPPED ? 1 : 0)
                .sum();

        logger.info("=== SUMMARY ===");
        logger.info("Components completed: {}", completed);
        logger.info("Components failed: {}", failed);
        logger.info("Components skipped: {}", skipped);

        if (result.getOverallStatus() == SystemStateManager.ComponentStatus.COMPLETED) {
            logger.info("🎉 System initialization completed successfully!");
            logger.info("Your system is now ready with the configured state.");
        } else {
            logger.warn("⚠️ System initialization completed with issues.");
            logger.info("Review the logs above for details on what failed or was skipped.");
        }

        logger.info("=== END INITIALIZATION REPORT ===");
    }

    private void logComponentDetails(ComponentResult result) {
        if (result.getCreatedEntities() != null && !result.getCreatedEntities().isEmpty()) {
            result.getCreatedEntities().forEach((type, id) ->
                    logger.debug("  Created {}: {}", type, id));
        }

        if (result.getDetails() != null && !result.getDetails().isEmpty()) {
            result.getDetails().forEach((key, value) ->
                    logger.debug("  {}: {}", key, value));
        }
    }
}