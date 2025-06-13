package com.sadna_market.market.InfrastructureLayer.Initialization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class SystemConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(SystemConfigLoader.class);

    @Value("${system.init.config-file:src/main/resources/system-config.yml}")
    private String configFilePath;

    public SystemConfig loadConfig() {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(configFilePath);

            @SuppressWarnings("unchecked")
            Map<String, Object> yamlData = yaml.load(inputStream);

            return parseSystemConfig(yamlData);

        } catch (Exception e) {
            logger.error("Failed to load configuration from {}: {}", configFilePath, e.getMessage());
            return createDefaultConfig();
        }
    }

    @SuppressWarnings("unchecked")
    private SystemConfig parseSystemConfig(Map<String, Object> yamlData) {
        SystemConfig config = new SystemConfig();

        Map<String, Object> initialization = (Map<String, Object>) yamlData.get("initialization");
        if (initialization != null) {
            // Parse mode
            String mode = (String) initialization.get("mode");
            if (mode != null) {
                config.setMode(SystemStateManager.InitializationMode.valueOf(mode.toUpperCase()));
            }

            // Parse failure action
            String onFailure = (String) initialization.get("on_failure");
            if (onFailure != null) {
                config.setOnFailure(SystemConfig.FailureAction.valueOf(onFailure.toUpperCase()));
            }

            // Parse rollback setting
            Boolean rollback = (Boolean) initialization.get("rollback_on_error");
            if (rollback != null) {
                config.setRollbackOnError(rollback);
            }

            // Parse components
            Map<String, Object> components = (Map<String, Object>) initialization.get("components");
            if (components != null) {
                config.setComponents(parseComponents(components));
            }
        }

        return config;
    }

    @SuppressWarnings("unchecked")
    private List<ComponentConfig> parseComponents(Map<String, Object> componentsData) {
        List<ComponentConfig> components = new ArrayList<>();

        for (Map.Entry<String, Object> entry : componentsData.entrySet()) {
            String componentId = entry.getKey();
            Map<String, Object> componentData = (Map<String, Object>) entry.getValue();

            ComponentConfig component = new ComponentConfig();
            component.setId(componentId);

            // Parse enabled flag
            Boolean enabled = (Boolean) componentData.get("enabled");
            if (enabled != null) {
                component.setEnabled(enabled);
            }

            // Parse force flag
            Boolean force = (Boolean) componentData.get("force");
            if (force != null) {
                component.setForce(force);
            }

            // Parse dependencies
            List<String> dependsOn = (List<String>) componentData.get("depends_on");
            if (dependsOn != null) {
                component.setDependsOn(dependsOn);
            }

            // Parse configuration
            Map<String, Object> config = (Map<String, Object>) componentData.get("config");
            if (config != null) {
                component.setConfig(config);
            }

            components.add(component);
        }

        return components;
    }

    private SystemConfig createDefaultConfig() {
        logger.info("Creating default configuration for your current scenario");

        SystemConfig config = new SystemConfig();
        config.setMode(SystemStateManager.InitializationMode.SELECTIVE);
        config.setOnFailure(SystemConfig.FailureAction.STOP);
        config.setRollbackOnError(true);

        // Create components for your scenario
        List<ComponentConfig> components = new ArrayList<>();

        // Admin setup
        ComponentConfig adminSetup = new ComponentConfig();
        adminSetup.setId("admin_setup");
        adminSetup.setEnabled(true);
        Map<String, Object> adminConfig = new HashMap<>();
        adminConfig.put("username", "u1");
        adminConfig.put("password", "Password123!");
        adminConfig.put("email", "u1@market.com");
        adminConfig.put("firstName", "System");
        adminConfig.put("lastName", "Administrator");
        adminSetup.setConfig(adminConfig);
        components.add(adminSetup);

        // User registration
        ComponentConfig userReg = new ComponentConfig();
        userReg.setId("user_registration");
        userReg.setEnabled(true);
        userReg.setDependsOn(Arrays.asList("admin_setup"));
        Map<String, Object> userConfig = new HashMap<>();
        List<Map<String, Object>> users = new ArrayList<>();

        for (int i = 2; i <= 6; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("username", "u" + i);
            user.put("password", "Password123!");
            user.put("email", "u" + i + "@market.com");
            user.put("firstName", "User");
            user.put("lastName", "Number" + i);
            users.add(user);
        }
        userConfig.put("users", users);
        userReg.setConfig(userConfig);
        components.add(userReg);

        // User login
        ComponentConfig userLogin = new ComponentConfig();
        userLogin.setId("user_login");
        userLogin.setEnabled(true);
        userLogin.setDependsOn(Arrays.asList("user_registration"));
        Map<String, Object> loginConfig = new HashMap<>();
        List<Map<String, Object>> logins = new ArrayList<>();
        Map<String, Object> u2Login = new HashMap<>();
        u2Login.put("username", "u2");
        u2Login.put("password", "Password123!");
        logins.add(u2Login);
        loginConfig.put("logins", logins);
        userLogin.setConfig(loginConfig);
        components.add(userLogin);

        // Store creation
        ComponentConfig storeCreation = new ComponentConfig();
        storeCreation.setId("store_creation");
        storeCreation.setEnabled(true);
        storeCreation.setDependsOn(Arrays.asList("user_login"));
        Map<String, Object> storeConfig = new HashMap<>();
        List<Map<String, Object>> stores = new ArrayList<>();
        Map<String, Object> store = new HashMap<>();
        store.put("name", "s1");
        store.put("owner", "u2");
        store.put("description", "Store s1 - Sample store created during initialization");
        store.put("address", "123 Market Street Sample City SC 12345");
        store.put("email", "s1@market.com");
        store.put("phone", "555-STORE-01");
        stores.add(store);
        storeConfig.put("stores", stores);
        storeCreation.setConfig(storeConfig);
        components.add(storeCreation);

        // Product management
        ComponentConfig productMgmt = new ComponentConfig();
        productMgmt.setId("product_management");
        productMgmt.setEnabled(true);
        productMgmt.setDependsOn(Arrays.asList("store_creation"));
        Map<String, Object> productConfig = new HashMap<>();
        List<Map<String, Object>> products = new ArrayList<>();
        Map<String, Object> product = new HashMap<>();
        product.put("username", "u2");
        product.put("store", "s1");
        product.put("name", "Bamba");
        product.put("description", "Delicious peanut snack - crispy and tasty");
        product.put("category", "Snacks");
        product.put("price", 30.0);
        product.put("quantity", 30);
        products.add(product);
        productConfig.put("products", products);
        productMgmt.setConfig(productConfig);
        components.add(productMgmt);

        // Role management
        ComponentConfig roleMgmt = new ComponentConfig();
        roleMgmt.setId("role_management");
        roleMgmt.setEnabled(true);
        roleMgmt.setDependsOn(Arrays.asList("product_management"));
        Map<String, Object> roleConfig = new HashMap<>();

        // Managers
        List<Map<String, Object>> managers = new ArrayList<>();
        Map<String, Object> manager = new HashMap<>();
        manager.put("appointer", "u2");
        manager.put("store", "s1");
        manager.put("username", "u3");
        manager.put("permissions", Arrays.asList("MANAGE_INVENTORY", "ADD_PRODUCT", "REMOVE_PRODUCT", "UPDATE_PRODUCT"));
        managers.add(manager);
        roleConfig.put("managers", managers);

        // Owners
        List<Map<String, Object>> owners = new ArrayList<>();
        Map<String, Object> owner1 = new HashMap<>();
        owner1.put("appointer", "u2");
        owner1.put("store", "s1");
        owner1.put("username", "u4");
        owners.add(owner1);

        Map<String, Object> owner2 = new HashMap<>();
        owner2.put("appointer", "u2");
        owner2.put("store", "s1");
        owner2.put("username", "u5");
        owners.add(owner2);

        roleConfig.put("owners", owners);
        roleMgmt.setConfig(roleConfig);
        components.add(roleMgmt);

        // User logout
        ComponentConfig userLogout = new ComponentConfig();
        userLogout.setId("user_logout");
        userLogout.setEnabled(true);
        userLogout.setDependsOn(Arrays.asList("role_management"));
        Map<String, Object> logoutConfig = new HashMap<>();
        logoutConfig.put("users", Arrays.asList("u2"));
        userLogout.setConfig(logoutConfig);
        components.add(userLogout);

        config.setComponents(components);
        return config;
    }
}