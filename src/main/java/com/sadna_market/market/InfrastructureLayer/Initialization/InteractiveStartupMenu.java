// ===================================================================
// InteractiveStartupMenu.java - Clean Version From Scratch
// ===================================================================

package com.sadna_market.market.InfrastructureLayer.Initialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "system.startup.menu.enabled", havingValue = "true", matchIfMissing = true)
public class InteractiveStartupMenu implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(InteractiveStartupMenu.class);

    @Autowired
    private SystemStateManager stateManager;

    @Autowired
    private SystemConfigLoader configLoader;

    @Override
    public void run(String... args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏪 SADNA MARKET - STARTUP MENU");
        System.out.println("=".repeat(60));

        Scanner scanner = new Scanner(System.in);

        while (true) {
            showMenu();
            String choice = scanner.nextLine().trim();

            try {
                if (handleChoice(choice, scanner)) {
                    break; // Exit menu
                }
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
            }
        }

        // Ask about frontend
        askAboutFrontend(scanner);

        System.out.println("🚀 Backend starting...");
        // Spring Boot continues running after this method exits
    }

    private void showMenu() {
        System.out.println("\n📋 Choose startup option:");
        System.out.println("[1] 🔄 Fresh Start (Clear DB + Initialize)");
        System.out.println("[2] 📊 Load from Database");
        System.out.println("[3] 🔍 Check Current State");
        System.out.println("[4] ⚡ Smart Init (Add missing only)");
        System.out.println("[5] 🚪 Exit");
        System.out.print("Your choice (1-5): ");
    }

    private boolean handleChoice(String choice, Scanner scanner) {
        switch (choice) {
            case "1":
                return handleFreshStart(scanner);
            case "2":
                return handleLoadFromDB();
            case "3":
                handleCheckState();
                return false;
            case "4":
                return handleSmartInit();
            case "5":
                System.out.println("👋 Goodbye!");
                System.exit(0);
                return true;
            default:
                System.out.println("❌ Invalid choice. Please enter 1-5.");
                return false;
        }
    }

    private boolean handleFreshStart(Scanner scanner) {
        System.out.println("\n🔄 FRESH START");
        System.out.println("⚠️  This will delete ALL data!");
        System.out.print("Type 'YES' to confirm: ");

        if (!"YES".equals(scanner.nextLine().trim())) {
            System.out.println("❌ Cancelled.");
            return false;
        }

        SystemConfig config = configLoader.loadConfig();
        config.setMode(SystemStateManager.InitializationMode.RESET_AND_INIT);

        InitializationResult result = stateManager.executeInitialization(config);
        showResult(result);

        return true;
    }

    private boolean handleLoadFromDB() {
        System.out.println("\n📊 Loading from existing database...");
        System.out.println("✅ No initialization needed.");
        return true;
    }

    private void handleCheckState() {
        System.out.println("\n🔍 Checking current state...");

        SystemConfig config = configLoader.loadConfig();
        config.setMode(SystemStateManager.InitializationMode.CHECK_ONLY);

        InitializationResult result = stateManager.executeInitialization(config);
        showStateCheck(result);

        System.out.println("\nPress Enter to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    private boolean handleSmartInit() {
        System.out.println("\n⚡ Smart initialization...");
        System.out.println("✅ Will only add missing data.");

        SystemConfig config = configLoader.loadConfig();
        config.setMode(SystemStateManager.InitializationMode.SELECTIVE);

        InitializationResult result = stateManager.executeInitialization(config);
        showResult(result);

        return true;
    }

    private void showResult(InitializationResult result) {
        System.out.println("\n📋 RESULTS:");
        System.out.println("Status: " + result.getOverallStatus());

        for (ComponentResult comp : result.getComponentResults()) {
            String emoji = comp.getStatus() == SystemStateManager.ComponentStatus.COMPLETED ? "✅" :
                    comp.getStatus() == SystemStateManager.ComponentStatus.FAILED ? "❌" : "⏭️";
            System.out.println("  " + emoji + " " + comp.getComponentId());
        }

        System.out.println("\nPress Enter to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    private void showStateCheck(InitializationResult result) {
        System.out.println("\n📊 CURRENT STATE:");

        for (ComponentResult comp : result.getComponentResults()) {
            String emoji = comp.getStatus() == SystemStateManager.ComponentStatus.COMPLETED ? "✅" : "❌";
            System.out.println("  " + emoji + " " + comp.getComponentId() + ": " + comp.getStatus());
        }
    }

    private void askAboutFrontend(Scanner scanner) {
        System.out.print("\n🌐 Open frontend in browser? (Y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.isEmpty() || response.equals("y") || response.equals("yes")) {
            // Start frontend in background thread so it doesn't block Spring Boot
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // Wait for backend to start
                    openFrontend();
                } catch (Exception e) {
                    logger.warn("Could not open frontend: {}", e.getMessage());
                }
            }).start();
        } else {
            System.out.println("💡 Frontend: http://localhost:5173");
            System.out.println("💡 Backend: http://localhost:8081");
        }
    }

    private void openFrontend() {
        try {
            // Try to start frontend first
            startFrontend();

            // Wait a bit
            Thread.sleep(5000);

            // Open browser
            String os = System.getProperty("os.name").toLowerCase();
            String url = "http://localhost:5173";

            if (os.contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + url);
            } else {
                Runtime.getRuntime().exec("xdg-open " + url);
            }

            logger.info("🌐 Frontend opened at: {}", url);

        } catch (Exception e) {
            logger.warn("Could not open frontend automatically: {}", e.getMessage());
            logger.info("Please open manually: http://localhost:5173");
        }
    }

    private void startFrontend() {
        try {
            java.io.File frontendDir = new java.io.File("./Frontend");
            if (!frontendDir.exists()) {
                logger.warn("Frontend directory not found: {}", frontendDir.getAbsolutePath());
                return;
            }

            ProcessBuilder pb = new ProcessBuilder();
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                pb.command("cmd", "/c", "npm run dev");
            } else {
                pb.command("bash", "-c", "npm run dev");
            }

            pb.directory(frontendDir);
            pb.start();

            logger.info("Frontend starting...");

        } catch (Exception e) {
            logger.warn("Could not start frontend: {}", e.getMessage());
        }
    }
}