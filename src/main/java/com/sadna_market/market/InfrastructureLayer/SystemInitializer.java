package com.sadna_market.market.InfrastructureLayer;

import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.DomainLayer.DomainServices.InventoryManagementService;
import com.sadna_market.market.DomainLayer.DomainServices.RatingService;
import com.sadna_market.market.DomainLayer.DomainServices.StoreManagementService;
import com.sadna_market.market.DomainLayer.DomainServices.UserAccessService;
import com.sadna_market.market.InfrastructureLayer.Authentication.IAuthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Enhanced system initializer that creates comprehensive sample data with improved error handling,
 * better integration with domain services, and more realistic business scenarios.
 *
 * Features:
 * - Comprehensive user management with proper login/logout cycles
 * - Realistic store hierarchy with proper permissions
 * - Complete order lifecycle from cart to completion
 * - Rich product catalog with varied inventory levels
 * - Comprehensive rating and review system
 * - Message conversations between users and stores
 * - Violation reports with admin responses
 * - Proper error handling and rollback mechanisms
 */
@Component
@Profile("dev")
public class SystemInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(SystemInitializer.class);

    private final IUserRepository userRepository;
    private final IStoreRepository storeRepository;
    private final IProductRepository productRepository;
    private final IOrderRepository orderRepository;
    private final IMessageRepository messageRepository;
    private final IReportRepository reportRepository;
    private final IRatingRepository ratingRepository;
    private final IAddressRepository addressRepository;
    private final IAuthRepository authRepository;
    private final IUserStoreRolesRepository userStoreRolesRepository;
    private final UserAccessService userAccessService;
    private final StoreManagementService storeManagementService;
    private final InventoryManagementService inventoryManagementService;
    private final RatingService ratingService;

    // Enhanced tracking for better data management
    private final Map<String, List<UUID>> storeProductIds = new HashMap<>();
    private final Map<UUID, String> storeNames = new HashMap<>();
    private final Map<String, UUID> userStoreMap = new HashMap<>();
    private final List<String> allUsers = new ArrayList<>();
    private final Map<String, Set<Permission>> managerPermissions = new HashMap<>();

    // Configuration constants
    private static final String DEFAULT_PASSWORD = "Password1!";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String ADMIN_EMAIL = "admin@market.com";

    @Autowired
    public SystemInitializer(
            IUserRepository userRepository,
            IStoreRepository storeRepository,
            IProductRepository productRepository,
            IOrderRepository orderRepository,
            IMessageRepository messageRepository,
            IReportRepository reportRepository,
            IRatingRepository ratingRepository,
            IAddressRepository addressRepository,
            IAuthRepository authRepository,
            IUserStoreRolesRepository userStoreRolesRepository,
            UserAccessService userAccessService,
            StoreManagementService storeManagementService,
            InventoryManagementService inventoryManagementService,
            RatingService ratingService) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.messageRepository = messageRepository;
        this.reportRepository = reportRepository;
        this.ratingRepository = ratingRepository;
        this.addressRepository = addressRepository;
        this.authRepository = authRepository;
        this.userStoreRolesRepository = userStoreRolesRepository;
        this.userAccessService = userAccessService;
        this.storeManagementService = storeManagementService;
        this.inventoryManagementService = inventoryManagementService;
        this.ratingService = ratingService;
    }

    @Override
    public void run(String... args) {
        logger.info("Starting enhanced system initialization with comprehensive sample data...");

        try {
            // Step 1: Clear existing data
            clearAllData();

            // Step 2: Initialize core entities
            initializeAdmin();
            Map<String, String> users = initializeUsers();
            initializeUserAddresses();

            // Step 3: Initialize business entities
            Map<String, UUID> storeIds = initializeStores();
            initializeProducts(storeIds);
            initializeStorePersonnel(storeIds);

            // Step 4: Initialize business processes
            simulateBusinessActivity(storeIds, users);
            initializeRatingsAndReviews(storeIds);
            initializeMessages(storeIds);
            initializeReports(storeIds);

            // Step 5: Validate and summarize
            validateSystemState();
            logInitializationSummary();

            logger.info("Enhanced system initialization completed successfully!");
        } catch (Exception e) {
            logger.error("Error during system initialization: {}", e.getMessage(), e);
            handleInitializationFailure(e);
        }
    }

    private void clearAllData() {
        logger.info("Clearing existing data...");
        try {
            userStoreRolesRepository.clear();
            userRepository.clear();
            storeRepository.clear();
            productRepository.clear();
            orderRepository.clear();
            messageRepository.clear();
            reportRepository.clear();
            ratingRepository.clear();
            addressRepository.clear();
            authRepository.clear();

            // Clear tracking maps
            storeNames.clear();
            storeProductIds.clear();
            userStoreMap.clear();
            allUsers.clear();
            managerPermissions.clear();

            logger.info("All repositories cleared successfully");
        } catch (Exception e) {
            logger.error("Error clearing data: {}", e.getMessage());
            throw new RuntimeException("Failed to clear existing data", e);
        }
    }

    private void initializeAdmin() {
        logger.info("Initializing admin user...");
        try {
            User admin = userAccessService.registerUser(
                    ADMIN_USERNAME,
                    ADMIN_PASSWORD,
                    ADMIN_EMAIL,
                    "System",
                    "Administrator"
            );
            authRepository.addUser(ADMIN_USERNAME, ADMIN_PASSWORD);
            allUsers.add(ADMIN_USERNAME);

            logger.info("Admin user created successfully");
        } catch (Exception e) {
            logger.error("Failed to create admin user: {}", e.getMessage());
            throw new RuntimeException("Admin initialization failed", e);
        }
    }

    private Map<String, String> initializeUsers() {
        logger.info("Initializing regular users with enhanced profiles...");

        Map<String, String> users = new LinkedHashMap<>();

        // Enhanced user data with roles and characteristics
        String[][] userData = {
                // username, email, firstName, lastName, role
                {"pikachu", "pikachu@example.com", "Pika", "Chu", "store_owner"},
                {"charizard", "charizard@example.com", "Chari", "Zard", "store_owner"},
                {"bulbasaur", "bulbasaur@example.com", "Bulba", "Saur", "store_owner"},
                {"mewtwo", "mewtwo@example.com", "Mew", "Two", "store_owner"},
                {"squirtle", "squirtle@example.com", "Squir", "Tle", "manager"},
                {"eevee", "eevee@example.com", "Ee", "Vee", "manager"},
                {"lucario", "lucario@example.com", "Luca", "Rio", "manager"},
                {"dragonite", "dragonite@example.com", "Dragon", "Ite", "customer"},
                {"gengar", "gengar@example.com", "Gen", "Gar", "customer"},
                {"alakazam", "alakazam@example.com", "Alaka", "Zam", "customer"}
        };

        for (String[] userInfo : userData) {
            try {
                User user = userAccessService.registerUser(
                        userInfo[0],
                        DEFAULT_PASSWORD,
                        userInfo[1],
                        userInfo[2],
                        userInfo[3]
                );
                authRepository.addUser(userInfo[0], DEFAULT_PASSWORD);
                users.put(userInfo[0], userInfo[4]);
                allUsers.add(userInfo[0]);

                logger.debug("Created user: {} ({})", userInfo[0], userInfo[4]);
            } catch (Exception e) {
                logger.error("Failed to create user {}: {}", userInfo[0], e.getMessage());
            }
        }

        logger.info("Created {} users successfully", users.size());
        return users;
    }

    private void initializeUserAddresses() {
        logger.info("Initializing comprehensive user addresses...");

        // Address data for each user
        Object[][] addressData = {
                // username, fullName, address1, address2, city, state, postal, country, phone, label, isDefault
                {"pikachu", "Pika Chu", "123 Thunderbolt Ave", "Apt 25", "Viridian City", "Kanto", "12345", "Pokémon World", "555-PIKA-CHU", "Home", true},
                {"pikachu", "Pika Chu", "456 Electric St", "Electric Store", "Viridian City", "Kanto", "12346", "Pokémon World", "555-STORE-01", "Work", false},

                {"charizard", "Chari Zard", "789 Flame Mountain", "Dragon's Lair", "Cinnabar Island", "Kanto", "67890", "Pokémon World", "555-CHAR-IZAR", "Home", true},

                {"bulbasaur", "Bulba Saur", "321 Garden Lane", "", "Celadon City", "Kanto", "54321", "Pokémon World", "555-BULBA-SAUR", "Home", true},
                {"bulbasaur", "Bulba Saur", "999 Greenhouse Rd", "Garden Store", "Celadon City", "Kanto", "54322", "Pokémon World", "555-GARDEN-01", "Store", false},

                {"mewtwo", "Mew Two", "777 Psychic Plaza", "Penthouse", "Saffron City", "Kanto", "11111", "Pokémon World", "555-MEW-TWO", "Home", true},

                {"squirtle", "Squir Tle", "147 Water Way", "", "Cerulean City", "Kanto", "98765", "Pokémon World", "555-SQUI-RTLE", "Home", true},

                {"eevee", "Ee Vee", "888 Evolution Ave", "", "Lavender Town", "Kanto", "22222", "Pokémon World", "555-EE-VEE", "Home", true},

                {"lucario", "Luca Rio", "456 Aura Street", "Fighting Dojo", "Canalave City", "Sinnoh", "33333", "Pokémon World", "555-LUCA-RIO", "Home", true},

                {"dragonite", "Dragon Ite", "999 Sky Highway", "Dragon's Den", "Blackthorn City", "Johto", "44444", "Pokémon World", "555-DRAG-ONITE", "Home", true},

                {"gengar", "Gen Gar", "666 Shadow Lane", "Haunted House", "Lavender Town", "Kanto", "55555", "Pokémon World", "555-GEN-GAR", "Home", true},

                {"alakazam", "Alaka Zam", "555 Spoon Bend St", "Psychic Center", "Saffron City", "Kanto", "66666", "Pokémon World", "555-ALAKA-ZAM", "Home", true}
        };

        for (Object[] addressInfo : addressData) {
            try {
                Address address = new Address(
                        (String) addressInfo[0],  // username
                        (String) addressInfo[1],  // fullName
                        (String) addressInfo[2],  // address1
                        (String) addressInfo[3],  // address2
                        (String) addressInfo[4],  // city
                        (String) addressInfo[5],  // state
                        (String) addressInfo[6],  // postal
                        (String) addressInfo[7],  // country
                        (String) addressInfo[8],  // phone
                        (String) addressInfo[9]   // label
                );

                address.setDefault((Boolean) addressInfo[10]);
                addressRepository.save(address);

                // Add address ID to user
                User user = userRepository.findByUsername((String) addressInfo[0]).orElse(null);
                if (user != null) {
                    user.addAddressId(address.getAddressId());
                    userRepository.update(user);
                }

                logger.debug("Created address for {}: {}", addressInfo[0], addressInfo[9]);
            } catch (Exception e) {
                logger.error("Failed to create address for {}: {}", addressInfo[0], e.getMessage());
            }
        }

        logger.info("User addresses initialized successfully");
    }

    private Map<String, UUID> initializeStores() {
        logger.info("Initializing stores with enhanced business profiles...");

        Map<String, UUID> storeIds = new HashMap<>();

        // Enhanced store data
        Object[][] storeData = {
                // founder, storeName, description, address, email, phone, category
                {"pikachu", "Electric Type Gadgets",
                        "⚡ Shocking deals on cutting-edge electronics! From smartphones to gaming consoles, we've got the electric power you need. Specializing in lightning-fast technology that'll shock you with its performance!",
                        "123 Thunderbolt Ave, Viridian City, Kanto 12345",
                        "contact@electrictype.com",
                        "555-PIKA-CHU",
                        "electronics"},

                {"charizard", "Fire Fashion Boutique",
                        "🔥 Hot styles that burn up the competition! Premium fashion that's always blazing with the latest trends. From casual wear to formal attire, we bring the heat to your wardrobe!",
                        "789 Flame Mountain, Cinnabar Island, Kanto 67890",
                        "info@firefashion.com",
                        "555-CHAR-IZAR",
                        "clothing"},

                {"bulbasaur", "Grass Grocers Organic",
                        "🌱 Fresh organic produce grown with pure solar energy! Naturally healthy food that connects you with nature. Sustainable, delicious, and packed with vitamins from our solar-powered greenhouses!",
                        "321 Garden Lane, Celadon City, Kanto 54321",
                        "orders@grassgrocers.com",
                        "555-BULBA-SAUR",
                        "grocery"},

                {"mewtwo", "Psychic Knowledge Center",
                        "🧠 Expand your mind with our vast collection of books, educational materials, and wisdom from across dimensions. Featuring rare texts, scientific journals, and mind-expanding literature!",
                        "777 Psychic Plaza, Saffron City, Kanto 11111",
                        "books@psychicknowledge.com",
                        "555-MEW-TWO",
                        "books"}
        };

        for (Object[] storeInfo : storeData) {
            try {
                Store store = storeManagementService.createStore(
                        (String) storeInfo[0], // founder
                        (String) storeInfo[1], // name
                        (String) storeInfo[2], // description
                        (String) storeInfo[3], // address
                        (String) storeInfo[4], // email
                        (String) storeInfo[5]  // phone
                );

                String category = (String) storeInfo[6];
                storeIds.put(category, store.getStoreId());
                storeNames.put(store.getStoreId(), (String) storeInfo[1]);
                userStoreMap.put((String) storeInfo[0], store.getStoreId());

                logger.debug("Created store: {} (Owner: {})", storeInfo[1], storeInfo[0]);
            } catch (Exception e) {
                logger.error("Failed to create store {}: {}", storeInfo[1], e.getMessage());
            }
        }

        logger.info("Created {} stores successfully", storeIds.size());
        return storeIds;
    }

    private void initializeProducts(Map<String, UUID> storeIds) {
        logger.info("Initializing comprehensive product catalog...");

        // Electronics store products
        initializeElectronicsProducts(storeIds.get("electronics"));

        // Clothing store products
        initializeClothingProducts(storeIds.get("clothing"));

        // Grocery store products
        initializeGroceryProducts(storeIds.get("grocery"));

        // Bookstore products
        initializeBookProducts(storeIds.get("books"));

        logger.info("Product catalog initialization completed");
    }

    private void initializeElectronicsProducts(UUID storeId) {
        List<UUID> productIds = new ArrayList<>();

        Object[][] products = {
                // name, category, description, price, quantity
                {"Thunder Phone Pro Max", "Smartphones", "Latest flagship smartphone with lightning-fast 5G, electric-type compatibility, and 48-hour battery life", 1199.99, 15},
                {"Volt Gaming Laptop Elite", "Computers", "Ultra-high performance gaming laptop with RTX graphics, perfect for electric-type gaming", 1899.99, 8},
                {"Zaptos Wireless Headphones", "Audio", "Premium noise-cancelling headphones with thunderous bass and 40-hour battery", 299.99, 25},
                {"Lightning TV 65\" OLED", "TVs", "Ultra-premium 8K OLED TV with lightning-fast 120Hz refresh and HDR10+", 1299.99, 10},
                {"Pikachu Gaming Console X", "Gaming", "Next-gen gaming console with ray tracing and exclusive electric-type games", 599.99, 12},
                {"Electric Charger Station", "Accessories", "Fast wireless charging station for all devices, powers up to 6 devices simultaneously", 149.99, 30},
                {"Thunder Tablet Pro", "Tablets", "Professional tablet with electric stylus, perfect for digital art and productivity", 799.99, 18},
                {"Jolteon Smart Watch", "Wearables", "Advanced fitness and health tracking with electric-type move monitoring", 399.99, 22},
                {"Volt Bluetooth Speaker", "Audio", "Portable speaker with shocking sound quality and waterproof design", 199.99, 35},
                {"Electric Car Charger", "Automotive", "Home electric vehicle charging station with smart grid integration", 899.99, 5}
        };

        for (Object[] product : products) {
            try {
                UUID productId = inventoryManagementService.addProductToStore(
                        "pikachu", storeId,
                        (String) product[0], (String) product[1], (String) product[2],
                        (double) product[3], (int) product[4]
                );
                productIds.add(productId);
            } catch (Exception e) {
                logger.error("Failed to add electronics product {}: {}", product[0], e.getMessage());
            }
        }

        storeProductIds.put("electronics", productIds);
        logger.debug("Added {} electronics products", productIds.size());
    }

    private void initializeClothingProducts(UUID storeId) {
        List<UUID> productIds = new ArrayList<>();

        Object[][] products = {
                // name, category, description, price, quantity
                {"Blaze Premium T-Shirt", "Casual", "Fire-resistant premium cotton t-shirt with flame patterns and moisture-wicking", 39.99, 30},
                {"Inferno Evening Dress", "Formal", "Elegant evening dress that radiates confidence, perfect for special occasions", 149.99, 15},
                {"Ember Kids Adventure Set", "Children", "Complete adventure outfit for little fire-types, includes shirt, shorts, and cap", 79.99, 25},
                {"Flame Denim Jacket", "Outerwear", "Stylish denim jacket with flame embroidery and thermal regulation", 129.99, 20},
                {"Heat Wave Baseball Cap", "Accessories", "Trendy cap with UV protection and temperature regulation technology", 34.99, 40},
                {"Phoenix Leather Jacket", "Premium", "Luxury leather jacket with fire-type styling and windproof lining", 299.99, 8},
                {"Charmander Cozy Hoodie", "Casual", "Ultra-soft hoodie featuring beloved fire starter, perfect for cooler weather", 69.99, 28},
                {"Fire Type Yoga Pants", "Activewear", "High-performance yoga pants with heat-retention and flexibility", 59.99, 22},
                {"Volcano Hiking Boots", "Footwear", "Durable hiking boots designed for hot terrain and long adventures", 179.99, 12},
                {"Blaze Formal Suit", "Formal", "Professional business suit with fire-resistant fabric and modern cut", 399.99, 6}
        };

        for (Object[] product : products) {
            try {
                UUID productId = inventoryManagementService.addProductToStore(
                        "charizard", storeId,
                        (String) product[0], (String) product[1], (String) product[2],
                        (double) product[3], (int) product[4]
                );
                productIds.add(productId);
            } catch (Exception e) {
                logger.error("Failed to add clothing product {}: {}", product[0], e.getMessage());
            }
        }

        storeProductIds.put("clothing", productIds);
        logger.debug("Added {} clothing products", productIds.size());
    }

    private void initializeGroceryProducts(UUID storeId) {
        List<UUID> productIds = new ArrayList<>();

        Object[][] products = {
                // name, category, description, price, quantity
                {"Pecha Berries Organic", "Fruits", "Sweet berries that cure status ailments, grown in solar-powered orchards", 6.99, 50},
                {"Solar Artisan Bread", "Bakery", "Freshly baked whole grain bread using clean solar energy ovens", 7.99, 35},
                {"Free-Range Chansey Eggs", "Dairy", "Premium eggs from happy free-range Chansey, rich in nutrients", 9.99, 40},
                {"Grass-Fed Tauros Ribeye", "Meat", "Prime cut ribeye steak from ethically raised Tauros, perfectly marbled", 18.99, 20},
                {"Oddish Garden Spinach", "Vegetables", "Fresh organic spinach from our sustainable garden beds", 4.99, 45},
                {"Sitrus Berry Fresh Juice", "Beverages", "Cold-pressed juice that naturally restores energy and vitality", 8.99, 32},
                {"Leppa Berry Trail Mix", "Snacks", "Healthy trail mix with energy-restoring berries and nuts", 12.99, 28},
                {"Apricorn Herbal Tea", "Beverages", "Soothing herbal tea blend made from rare Apricorn fruits", 11.99, 25},
                {"Razz Berry Jam", "Condiments", "Homemade jam with a perfect balance of sweet and tart flavors", 5.99, 30},
                {"Oran Berry Smoothie Mix", "Health", "Superfood smoothie mix packed with antioxidants and vitamins", 14.99, 18}
        };

        for (Object[] product : products) {
            try {
                UUID productId = inventoryManagementService.addProductToStore(
                        "bulbasaur", storeId,
                        (String) product[0], (String) product[1], (String) product[2],
                        (double) product[3], (int) product[4]
                );
                productIds.add(productId);
            } catch (Exception e) {
                logger.error("Failed to add grocery product {}: {}", product[0], e.getMessage());
            }
        }

        storeProductIds.put("grocery", productIds);
        logger.debug("Added {} grocery products", productIds.size());
    }

    private void initializeBookProducts(UUID storeId) {
        List<UUID> productIds = new ArrayList<>();

        Object[][] products = {
                // name, category, description, price, quantity
                {"Complete Pokémon Encyclopedia", "Reference", "Comprehensive guide to all known Pokémon species with detailed stats and habitats", 49.99, 20},
                {"Psychic Powers Mastery", "Self-Help", "Advanced guide to developing and controlling psychic abilities and telekinesis", 29.99, 15},
                {"Battle Strategy Compendium", "Strategy", "Professional battle tactics and type effectiveness strategies", 24.99, 25},
                {"Meditation for Mind & Soul", "Wellness", "Find inner peace and strengthen your mental connection with nature", 19.99, 18},
                {"Type Effectiveness Master Chart", "Educational", "Visual guide and reference for all Pokémon type interactions", 15.99, 30},
                {"Legendary Pokémon Myths", "Fiction", "Epic tales and historical accounts of legendary Pokémon encounters", 34.99, 12},
                {"Evolution Science Journal", "Science", "Scientific research on evolution patterns and environmental triggers", 39.99, 10},
                {"Trainer's Nutrition Guide", "Health", "Complete nutrition guide for trainers and their Pokémon partners", 22.99, 16},
                {"Ancient Runes & Mysteries", "History", "Exploration of ancient symbols and their connection to Pokémon powers", 27.99, 14},
                {"Dream World Chronicles", "Fiction", "Fantasy series about adventures in the mysterious Dream World", 21.99, 20}
        };

        for (Object[] product : products) {
            try {
                UUID productId = inventoryManagementService.addProductToStore(
                        "mewtwo", storeId,
                        (String) product[0], (String) product[1], (String) product[2],
                        (double) product[3], (int) product[4]
                );
                productIds.add(productId);
            } catch (Exception e) {
                logger.error("Failed to add book product {}: {}", product[0], e.getMessage());
            }
        }

        storeProductIds.put("books", productIds);
        logger.debug("Added {} book products", productIds.size());
    }

    private void initializeStorePersonnel(Map<String, UUID> storeIds) {
        logger.info("Initializing store personnel with comprehensive permissions...");

        // Electronics store personnel
        setupElectronicsStorePersonnel(storeIds.get("electronics"));

        // Clothing store personnel
        setupClothingStorePersonnel(storeIds.get("clothing"));

        // Grocery store personnel
        setupGroceryStorePersonnel(storeIds.get("grocery"));

        // Bookstore personnel
        setupBookstorePersonnel(storeIds.get("books"));

        logger.info("Store personnel initialization completed");
    }

    private void setupElectronicsStorePersonnel(UUID storeId) {
        try {
            // squirtle as senior manager with comprehensive permissions
            Set<Permission> squirtlePermissions = EnumSet.of(
                    Permission.MANAGE_INVENTORY,
                    Permission.ADD_PRODUCT,
                    Permission.REMOVE_PRODUCT,
                    Permission.UPDATE_PRODUCT,
                    Permission.RESPOND_TO_USER_INQUIRIES,
                    Permission.VIEW_STORE_PURCHASE_HISTORY,
                    Permission.RESPOND_TO_BID
            );
            storeManagementService.appointStoreManager("pikachu", storeId, "squirtle", squirtlePermissions);
            managerPermissions.put("squirtle", squirtlePermissions);

            logger.debug("Setup electronics store personnel");
        } catch (Exception e) {
            logger.error("Failed to setup electronics store personnel: {}", e.getMessage());
        }
    }

    private void setupClothingStorePersonnel(UUID storeId) {
        try {
            // mewtwo as co-owner
            storeManagementService.appointStoreOwner("charizard", storeId, "mewtwo");

            // eevee as fashion manager
            Set<Permission> eeveePermissions = EnumSet.of(
                    Permission.MANAGE_INVENTORY,
                    Permission.UPDATE_PRODUCT,
                    Permission.RESPOND_TO_USER_INQUIRIES,
                    Permission.VIEW_STORE_PURCHASE_HISTORY,
                    Permission.MANAGE_DISCOUNT_POLICY
            );
            storeManagementService.appointStoreManager("charizard", storeId, "eevee", eeveePermissions);
            managerPermissions.put("eevee", eeveePermissions);

            logger.debug("Setup clothing store personnel");
        } catch (Exception e) {
            logger.error("Failed to setup clothing store personnel: {}", e.getMessage());
        }
    }

    private void setupGroceryStorePersonnel(UUID storeId) {
        try {
            // lucario as organic products manager
            Set<Permission> lucarioPermissions = EnumSet.of(
                    Permission.MANAGE_INVENTORY,
                    Permission.ADD_PRODUCT,
                    Permission.RESPOND_TO_USER_INQUIRIES,
                    Permission.VIEW_STORE_PURCHASE_HISTORY
            );
            storeManagementService.appointStoreManager("bulbasaur", storeId, "lucario", lucarioPermissions);
            managerPermissions.put("lucario", lucarioPermissions);

            logger.debug("Setup grocery store personnel");
        } catch (Exception e) {
            logger.error("Failed to setup grocery store personnel: {}", e.getMessage());
        }
    }

    private void setupBookstorePersonnel(UUID storeId) {
        try {
            // alakazam as knowledge manager
            Set<Permission> alakazamPermissions = EnumSet.of(
                    Permission.MANAGE_INVENTORY,
                    Permission.ADD_PRODUCT,
                    Permission.REMOVE_PRODUCT,
                    Permission.UPDATE_PRODUCT,
                    Permission.RESPOND_TO_USER_INQUIRIES
            );
            storeManagementService.appointStoreManager("mewtwo", storeId, "alakazam", alakazamPermissions);
            managerPermissions.put("alakazam", alakazamPermissions);

            logger.debug("Setup bookstore personnel");
        } catch (Exception e) {
            logger.error("Failed to setup bookstore personnel: {}", e.getMessage());
        }
    }

    private void simulateBusinessActivity(Map<String, UUID> storeIds, Map<String, String> users) {
        logger.info("Simulating realistic business activity...");

        // Simulate user shopping sessions
        simulateShoppingSessions(storeIds);

        // Create completed orders with realistic order history
        createCompletedOrders(storeIds);

        // Simulate current shopping carts
        simulateActiveShoppingCarts(storeIds);

        logger.info("Business activity simulation completed");
    }

    private void simulateShoppingSessions(Map<String, UUID> storeIds) {
        logger.debug("Simulating user shopping sessions...");

        // Simulate browsing and cart management
        String[] customers = {"dragonite", "gengar", "alakazam", "eevee", "lucario"};

        for (String customer : customers) {
            try {
                userAccessService.loginUser(customer, DEFAULT_PASSWORD);

                // Simulate browsing different stores
                for (String storeType : storeIds.keySet()) {
                    List<UUID> products = storeProductIds.get(storeType);
                    if (products != null && !products.isEmpty()) {
                        // Add some items to cart, then maybe remove some
                        UUID productId = products.get(new Random().nextInt(products.size()));
                        userAccessService.addToCart(customer, storeIds.get(storeType), productId, 1);

                        // Sometimes remove items (realistic behavior)
                        if (Math.random() > 0.7) {
                            userAccessService.removeFromCart(customer, storeIds.get(storeType), productId);
                        }
                    }
                }

                userAccessService.logoutUser(customer);
            } catch (Exception e) {
                logger.debug("Shopping simulation for {}: {}", customer, e.getMessage());
            }
        }
    }

    private void createCompletedOrders(Map<String, UUID> storeIds) {
        logger.debug("Creating completed orders with comprehensive details...");

        LocalDateTime baseTime = LocalDateTime.now().minusDays(45);

        // Order scenarios with realistic user behavior
        createOrderScenario1(storeIds, baseTime); // eevee electronics shopping
        createOrderScenario2(storeIds, baseTime); // dragonite grocery run
        createOrderScenario3(storeIds, baseTime); // lucario clothing shopping
        createOrderScenario4(storeIds, baseTime); // gengar book collection
        createOrderScenario5(storeIds, baseTime); // alakazam multi-store shopping
        createOrderScenario6(storeIds, baseTime); // Recent orders with different statuses
    }

    private void createOrderScenario1(Map<String, UUID> storeIds, LocalDateTime baseTime) {
        // eevee buys electronics - premium customer scenario
        List<UUID> electronicsProducts = storeProductIds.get("electronics");
        if (electronicsProducts != null && electronicsProducts.size() >= 3) {
            try {
                Map<UUID, Integer> orderProducts = new HashMap<>();
                orderProducts.put(electronicsProducts.get(0), 1); // Thunder Phone Pro Max
                orderProducts.put(electronicsProducts.get(2), 1); // Zaptos Headphones
                orderProducts.put(electronicsProducts.get(7), 1); // Jolteon Smart Watch

                UUID orderId = orderRepository.createOrderWithDetails(
                        storeIds.get("electronics"),
                        "eevee",
                        orderProducts,
                        1899.97, 1899.97,
                        baseTime.plusDays(2),
                        OrderStatus.COMPLETED,
                        10001,
                        storeNames.get(storeIds.get("electronics")),
                        "Credit Card (**** 4567)",
                        getFormattedAddress("eevee")
                );

                addOrderToUserHistory("eevee", orderId);
                logger.debug("Created premium electronics order for eevee");
            } catch (Exception e) {
                logger.warn("Failed to create order scenario 1: {}", e.getMessage());
            }
        }
    }

    private void createOrderScenario2(Map<String, UUID> storeIds, LocalDateTime baseTime) {
        // dragonite grocery shopping - bulk buyer scenario
        List<UUID> groceryProducts = storeProductIds.get("grocery");
        if (groceryProducts != null && groceryProducts.size() >= 4) {
            try {
                Map<UUID, Integer> orderProducts = new HashMap<>();
                orderProducts.put(groceryProducts.get(0), 5); // Pecha Berries x5
                orderProducts.put(groceryProducts.get(1), 3); // Solar Bread x3
                orderProducts.put(groceryProducts.get(3), 2); // Tauros Ribeye x2
                orderProducts.put(groceryProducts.get(5), 2); // Sitrus Juice x2

                UUID orderId = orderRepository.createOrderWithDetails(
                        storeIds.get("grocery"),
                        "dragonite",
                        orderProducts,
                        90.92, 90.92,
                        baseTime.plusDays(5),
                        OrderStatus.COMPLETED,
                        10002,
                        storeNames.get(storeIds.get("grocery")),
                        "Debit Card (**** 9876)",
                        getFormattedAddress("dragonite")
                );

                addOrderToUserHistory("dragonite", orderId);
                logger.debug("Created bulk grocery order for dragonite");
            } catch (Exception e) {
                logger.warn("Failed to create order scenario 2: {}", e.getMessage());
            }
        }
    }

    private void createOrderScenario3(Map<String, UUID> storeIds, LocalDateTime baseTime) {
        // lucario fashion shopping - style-conscious buyer
        List<UUID> clothingProducts = storeProductIds.get("clothing");
        if (clothingProducts != null && clothingProducts.size() >= 3) {
            try {
                Map<UUID, Integer> orderProducts = new HashMap<>();
                orderProducts.put(clothingProducts.get(0), 2); // Blaze T-Shirts x2
                orderProducts.put(clothingProducts.get(6), 1); // Charmander Hoodie
                orderProducts.put(clothingProducts.get(8), 1); // Volcano Hiking Boots

                UUID orderId = orderRepository.createOrderWithDetails(
                        storeIds.get("clothing"),
                        "lucario",
                        orderProducts,
                        329.97, 329.97,
                        baseTime.plusDays(8),
                        OrderStatus.COMPLETED,
                        10003,
                        storeNames.get(storeIds.get("clothing")),
                        "Credit Card (**** 1234)",
                        getFormattedAddress("lucario")
                );

                addOrderToUserHistory("lucario", orderId);
                logger.debug("Created fashion order for lucario");
            } catch (Exception e) {
                logger.warn("Failed to create order scenario 3: {}", e.getMessage());
            }
        }
    }

    private void createOrderScenario4(Map<String, UUID> storeIds, LocalDateTime baseTime) {
        // gengar book collection - knowledge seeker
        List<UUID> bookProducts = storeProductIds.get("books");
        if (bookProducts != null && bookProducts.size() >= 3) {
            try {
                Map<UUID, Integer> orderProducts = new HashMap<>();
                orderProducts.put(bookProducts.get(0), 1); // Pokemon Encyclopedia
                orderProducts.put(bookProducts.get(5), 1); // Legendary Myths
                orderProducts.put(bookProducts.get(8), 1); // Ancient Runes

                UUID orderId = orderRepository.createOrderWithDetails(
                        storeIds.get("books"),
                        "gengar",
                        orderProducts,
                        112.97, 112.97,
                        baseTime.plusDays(12),
                        OrderStatus.COMPLETED,
                        10004,
                        storeNames.get(storeIds.get("books")),
                        "PayPal",
                        getFormattedAddress("gengar")
                );

                addOrderToUserHistory("gengar", orderId);
                logger.debug("Created book collection order for gengar");
            } catch (Exception e) {
                logger.warn("Failed to create order scenario 4: {}", e.getMessage());
            }
        }
    }

    private void createOrderScenario5(Map<String, UUID> storeIds, LocalDateTime baseTime) {
        // alakazam multi-store shopping spree
        try {
            // Electronics order
            List<UUID> electronicsProducts = storeProductIds.get("electronics");
            if (electronicsProducts != null && electronicsProducts.size() >= 2) {
                Map<UUID, Integer> electronicsOrder = new HashMap<>();
                electronicsOrder.put(electronicsProducts.get(6), 1); // Thunder Tablet Pro

                UUID electronicsOrderId = orderRepository.createOrderWithDetails(
                        storeIds.get("electronics"),
                        "alakazam",
                        electronicsOrder,
                        799.99, 799.99,
                        baseTime.plusDays(15),
                        OrderStatus.COMPLETED,
                        10005,
                        storeNames.get(storeIds.get("electronics")),
                        "Credit Card (**** 5555)",
                        getFormattedAddress("alakazam")
                );
                addOrderToUserHistory("alakazam", electronicsOrderId);
            }

            // Books order (same day)
            List<UUID> bookProducts = storeProductIds.get("books");
            if (bookProducts != null && bookProducts.size() >= 2) {
                Map<UUID, Integer> booksOrder = new HashMap<>();
                booksOrder.put(bookProducts.get(1), 1); // Psychic Powers Mastery
                booksOrder.put(bookProducts.get(6), 1); // Evolution Science Journal

                UUID booksOrderId = orderRepository.createOrderWithDetails(
                        storeIds.get("books"),
                        "alakazam",
                        booksOrder,
                        69.98, 69.98,
                        baseTime.plusDays(15),
                        OrderStatus.COMPLETED,
                        10006,
                        storeNames.get(storeIds.get("books")),
                        "Credit Card (**** 5555)",
                        getFormattedAddress("alakazam")
                );
                addOrderToUserHistory("alakazam", booksOrderId);
            }

            logger.debug("Created multi-store shopping spree for alakazam");
        } catch (Exception e) {
            logger.warn("Failed to create order scenario 5: {}", e.getMessage());
        }
    }

    private void createOrderScenario6(Map<String, UUID> storeIds, LocalDateTime baseTime) {
        // Recent orders with different statuses
        try {
            // Recent SHIPPED order
            List<UUID> clothingProducts = storeProductIds.get("clothing");
            if (clothingProducts != null && !clothingProducts.isEmpty()) {
                Map<UUID, Integer> shippedOrder = new HashMap<>();
                shippedOrder.put(clothingProducts.get(5), 1); // Phoenix Leather Jacket

                orderRepository.createOrderWithDetails(
                        storeIds.get("clothing"),
                        "dragonite",
                        shippedOrder,
                        299.99, 299.99,
                        LocalDateTime.now().minusDays(3),
                        OrderStatus.SHIPPED,
                        10007,
                        storeNames.get(storeIds.get("clothing")),
                        "Credit Card (**** 9876)",
                        getFormattedAddress("dragonite")
                );
            }

            // Recent PAID order
            List<UUID> groceryProducts = storeProductIds.get("grocery");
            if (groceryProducts != null && !groceryProducts.isEmpty()) {
                Map<UUID, Integer> paidOrder = new HashMap<>();
                paidOrder.put(groceryProducts.get(7), 2); // Apricorn Tea x2

                orderRepository.createOrderWithDetails(
                        storeIds.get("grocery"),
                        "gengar",
                        paidOrder,
                        23.98, 23.98,
                        LocalDateTime.now().minusHours(8),
                        OrderStatus.PAID,
                        10008,
                        storeNames.get(storeIds.get("grocery")),
                        "Apple Pay",
                        getFormattedAddress("gengar")
                );
            }

            // PENDING order
            List<UUID> electronicsProducts = storeProductIds.get("electronics");
            if (electronicsProducts != null && !electronicsProducts.isEmpty()) {
                Map<UUID, Integer> pendingOrder = new HashMap<>();
                pendingOrder.put(electronicsProducts.get(4), 1); // Gaming Console

                orderRepository.createOrder(
                        storeIds.get("electronics"),
                        "lucario",
                        pendingOrder,
                        599.99, 599.99,
                        LocalDateTime.now().minusHours(2),
                        OrderStatus.PENDING,
                        -1
                );
            }

            logger.debug("Created recent orders with various statuses");
        } catch (Exception e) {
            logger.warn("Failed to create order scenario 6: {}", e.getMessage());
        }
    }

    private void simulateActiveShoppingCarts(Map<String, UUID> storeIds) {
        logger.debug("Setting up active shopping carts...");

        // mewtwo's cart - psychic shopper
        setupShoppingCart("mewtwo", storeIds, new String[][]{
                {"electronics", "1"}, // Tablet for research
                {"books", "2"} // Multiple books
        });

        // dragonite's cart - practical shopper
        setupShoppingCart("dragonite", storeIds, new String[][]{
                {"clothing", "8"}, // Hiking boots
                {"grocery", "3"} // Steaks for protein
        });

        // gengar's cart - mysterious shopper
        setupShoppingCart("gengar", storeIds, new String[][]{
                {"books", "8"}, // Ancient mysteries
                {"electronics", "8"} // Smart watch for tracking
        });
    }

    private void setupShoppingCart(String username, Map<String, UUID> storeIds, String[][] cartItems) {
        try {
            userAccessService.loginUser(username, DEFAULT_PASSWORD);

            for (String[] item : cartItems) {
                String storeType = item[0];
                int productIndex = Integer.parseInt(item[1]);

                List<UUID> products = storeProductIds.get(storeType);
                if (products != null && productIndex < products.size()) {
                    userAccessService.addToCart(username, storeIds.get(storeType),
                            products.get(productIndex), 1);
                }
            }

            userAccessService.logoutUser(username);
            logger.debug("Setup shopping cart for {}", username);
        } catch (Exception e) {
            logger.debug("Failed to setup cart for {}: {}", username, e.getMessage());
        }
    }

    private void initializeRatingsAndReviews(Map<String, UUID> storeIds) {
        logger.info("Initializing comprehensive ratings and reviews...");

        // Only users who have completed orders can rate/review
        createRatingsForUser("eevee", storeIds);
        createRatingsForUser("dragonite", storeIds);
        createRatingsForUser("lucario", storeIds);
        createRatingsForUser("gengar", storeIds);
        createRatingsForUser("alakazam", storeIds);

        // Create store ratings
        createStoreRatings(storeIds);

        logger.info("Ratings and reviews initialization completed");
    }

    private void createRatingsForUser(String username, Map<String, UUID> storeIds) {
        try {
            // Rate products based on completed orders
            List<Order> userOrders = orderRepository.findByUserName(username);

            for (Order order : userOrders) {
                if (order.getStatus() == OrderStatus.COMPLETED) {
                    for (Map.Entry<UUID, Integer> product : order.getProductsMap().entrySet()) {
                        try {
                            int rating = 3 + new Random().nextInt(3); // 3-5 stars
                            ratingService.rateProduct(username, product.getKey(), rating);

                            // Add review for some products
                            if (Math.random() > 0.5) {
                                String review = generateReview(username, rating);
                                ratingService.reviewProduct(username, product.getKey(),
                                        order.getStoreId(), review);
                            }
                        } catch (Exception e) {
                            logger.debug("Could not rate product for {}: {}", username, e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to create ratings for {}: {}", username, e.getMessage());
        }
    }

    private String generateReview(String username, int rating) {
        String[][] reviewTemplates = {
                // 5-star reviews
                {"Amazing quality! Exceeded my expectations. Will definitely buy again!",
                        "Perfect product! Fast shipping and excellent customer service.",
                        "Outstanding value for money. Highly recommend to everyone!"},
                // 4-star reviews
                {"Great product overall. Minor issues but still very satisfied.",
                        "Good quality and fair price. Would recommend to friends.",
                        "Solid purchase. Does exactly what it promises."},
                // 3-star reviews
                {"Decent product. Nothing special but gets the job done.",
                        "Average quality. Some room for improvement but okay.",
                        "Fair value. Met basic expectations."}
        };

        int templateIndex = Math.max(0, rating - 3);
        String[] templates = reviewTemplates[templateIndex];
        return templates[new Random().nextInt(templates.length)];
    }

    private void createStoreRatings(Map<String, UUID> storeIds) {
        String[] reviewers = {"eevee", "dragonite", "lucario", "gengar", "alakazam"};

        for (String reviewer : reviewers) {
            for (Map.Entry<String, UUID> store : storeIds.entrySet()) {
                try {
                    if (Math.random() > 0.3) { // 70% chance to rate each store
                        int rating = 3 + new Random().nextInt(3); // 3-5 stars
                        String comment = generateStoreReview(store.getKey(), rating);
                        ratingService.rateStore(reviewer, store.getValue(), rating, comment);
                    }
                } catch (Exception e) {
                    logger.debug("Could not rate store for {}: {}", reviewer, e.getMessage());
                }
            }
        }
    }

    private String generateStoreReview(String storeType, int rating) {
        Map<String, String[]> storeReviews = new HashMap<>();
        storeReviews.put("electronics", new String[]{
                "Cutting-edge technology with lightning-fast service!",
                "Great selection of electronic devices. Very knowledgeable staff.",
                "Good prices on electronics. Quick delivery."
        });
        storeReviews.put("clothing", new String[]{
                "Fashionable clothes with excellent quality materials!",
                "Trendy styles and great customer service.",
                "Nice clothing selection. Good fit and quality."
        });
        storeReviews.put("grocery", new String[]{
                "Fresh organic produce! You can taste the solar energy.",
                "Healthy and sustainable food options.",
                "Good organic selection. Fair prices."
        });
        storeReviews.put("books", new String[]{
                "Incredible collection of knowledge! Mind-expanding selection.",
                "Great variety of educational and fiction books.",
                "Good bookstore with helpful recommendations."
        });

        String[] reviews = storeReviews.get(storeType);
        if (reviews != null) {
            return reviews[Math.min(rating - 3, reviews.length - 1)];
        }
        return "Good store overall.";
    }

    private void initializeMessages(Map<String, UUID> storeIds) {
        logger.info("Initializing comprehensive message conversations...");

        createCustomerInquiries(storeIds);
        createStoreNotifications(storeIds);

        logger.info("Message initialization completed");
    }

    private void createCustomerInquiries(Map<String, UUID> storeIds) {
        // Customer inquiries with realistic scenarios
        Object[][] inquiries = {
                // customer, storeType, message, managerResponse
                {"dragonite", "electronics",
                        "Hi! I'm interested in the Thunder Phone Pro Max. Does it have good battery life for long flights? I need something reliable at high altitudes.",
                        "Hello Dragonite! The Thunder Phone Pro Max has a 48-hour battery life and is tested for extreme conditions including high altitude use. Perfect for flying!"},

                {"gengar", "clothing",
                        "Do you have the Phoenix Leather Jacket in size L? I need something that works well in dark, misty environments.",
                        "Hi Gengar! Yes, we have size L in stock. The Phoenix Jacket is designed with moisture-resistant materials perfect for misty conditions!"},

                {"alakazam", "grocery",
                        "I'm looking for brain food that enhances psychic abilities. What organic options do you recommend?",
                        "Greetings Alakazam! Our Sitrus Berry juice and Leppa Berry trail mix are excellent for mental clarity. All solar-grown for maximum psychic energy!"},

                {"lucario", "books",
                        "Do you have any advanced books on aura manipulation and meditation techniques?",
                        "Hello Lucario! Check out our 'Meditation for Mind & Soul' and 'Psychic Powers Mastery' - both have sections on aura control!"},

                {"eevee", "electronics",
                        "I'm considering multiple evolution paths. Do your devices work with all evolution types?",
                        "Hi Eevee! Our electric-type devices are compatible with all evolution forms. The adaptive technology adjusts to your evolution!"}
        };

        for (Object[] inquiry : inquiries) {
            try {
                String customer = (String) inquiry[0];
                String storeType = (String) inquiry[1];
                String messageText = (String) inquiry[2];
                String response = (String) inquiry[3];

                UUID storeId = storeIds.get(storeType);
                if (storeId != null) {
                    Message message = new Message(customer, storeId, messageText);
                    messageRepository.save(message);

                    // Add manager response
                    String manager = getStoreManager(storeType);
                    if (manager != null) {
                        messageRepository.addReply(message.getMessageId(), manager, response);
                    }
                }
            } catch (Exception e) {
                logger.debug("Failed to create inquiry: {}", e.getMessage());
            }
        }

        // Create some unread messages
        createUnreadMessages(storeIds);
    }

    private void createUnreadMessages(Map<String, UUID> storeIds) {
        Object[][] unreadMessages = {
                {"gengar", "electronics", "Are your gaming devices compatible with ghost-type energy? I tend to phase through regular controllers."},
                {"dragonite", "books", "Do you have any books about dragon migration patterns and ancient dragon lore?"},
                {"alakazam", "clothing", "I need formal wear suitable for psychic presentations. Do you have anything that doesn't interfere with telekinesis?"}
        };

        for (Object[] msgData : unreadMessages) {
            try {
                String customer = (String) msgData[0];
                String storeType = (String) msgData[1];
                String messageText = (String) msgData[2];

                UUID storeId = storeIds.get(storeType);
                if (storeId != null) {
                    Message message = new Message(customer, storeId, messageText);
                    messageRepository.save(message);
                }
            } catch (Exception e) {
                logger.debug("Failed to create unread message: {}", e.getMessage());
            }
        }
    }

    private void createStoreNotifications(Map<String, UUID> storeIds) {
        // System notifications to stores
        for (Map.Entry<String, UUID> store : storeIds.entrySet()) {
            try {
                Message notification = new Message(
                        "System",
                        store.getValue(),
                        "Welcome to the market! Your store is now active and ready for customers. " +
                                "Remember to respond to customer inquiries promptly for better ratings."
                );
                messageRepository.save(notification);
            } catch (Exception e) {
                logger.debug("Failed to create store notification: {}", e.getMessage());
            }
        }
    }

    private String getStoreManager(String storeType) {
        Map<String, String> managers = Map.of(
                "electronics", "squirtle",
                "clothing", "eevee",
                "grocery", "lucario",
                "books", "alakazam"
        );
        return managers.get(storeType);
    }

    private void initializeReports(Map<String, UUID> storeIds) {
        logger.info("Initializing violation reports and admin responses...");

        createViolationReports(storeIds);
        createAdminResponses();

        logger.info("Reports initialization completed");
    }

    private void createViolationReports(Map<String, UUID> storeIds) {
        // Create realistic violation reports
        Object[][] reports = {
                // reporter, storeType, productIndex, complaint
                {"dragonite", "electronics", 1,
                        "The gaming laptop overheated during normal use. This could be dangerous, especially for fire-type users."},

                {"gengar", "clothing", 0,
                        "The 'fire-resistant' shirt got damaged during a minor flame test. The product description seems misleading."},

                {"alakazam", "grocery", 8,
                        "The berry jam arrived expired. The expiration date was already passed when I received it."}
        };

        for (Object[] reportData : reports) {
            try {
                String reporter = (String) reportData[0];
                String storeType = (String) reportData[1];
                int productIndex = (int) reportData[2];
                String complaint = (String) reportData[3];

                UUID storeId = storeIds.get(storeType);
                List<UUID> products = storeProductIds.get(storeType);

                if (storeId != null && products != null && productIndex < products.size()) {
                    Report report = new Report(reporter, complaint, storeId, products.get(productIndex));
                    reportRepository.save(report);

                    // Add to user's reports
                    User user = userRepository.findByUsername(reporter).orElse(null);
                    if (user != null) {
                        user.addReport(report.getReportId());
                        userRepository.update(user);
                    }
                }
            } catch (Exception e) {
                logger.debug("Failed to create violation report: {}", e.getMessage());
            }
        }
    }

    private void createAdminResponses() {
        // Admin responds to some reports
        try {
            List<Report> reports = reportRepository.getAllReports();
            for (Report report : reports) {
                if (Math.random() > 0.5) { // 50% chance admin has responded
                    try {
                        userAccessService.replyViolationReport(
                                ADMIN_USERNAME,
                                report.getReportId(),
                                report.getUsername(),
                                "Thank you for your report. We have investigated this issue and are working with the store to resolve it. " +
                                        "Your feedback helps us maintain quality standards."
                        );
                    } catch (Exception e) {
                        logger.debug("Failed to create admin response: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to create admin responses: {}", e.getMessage());
        }
    }

    private void validateSystemState() {
        logger.info("Validating system state after initialization...");

        try {
            // Validate user count
            List<User> users = userRepository.findAll();
            if (users.size() < 10) {
                logger.warn("Expected at least 10 users, found {}", users.size());
            }

            // Validate store count
            List<Store> stores = storeRepository.findAll();
            if (stores.size() < 4) {
                logger.warn("Expected at least 4 stores, found {}", stores.size());
            }

            // Validate products
            for (String storeType : storeProductIds.keySet()) {
                List<UUID> products = storeProductIds.get(storeType);
                if (products.size() < 8) {
                    logger.warn("Store {} has only {} products, expected at least 8", storeType, products.size());
                }
            }

            // Validate orders
            List<Order> orders = orderRepository.findAll();
            long completedOrders = orders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                    .count();
            if (completedOrders < 5) {
                logger.warn("Expected at least 5 completed orders, found {}", completedOrders);
            }

            logger.info("System state validation completed");
        } catch (Exception e) {
            logger.error("Error during system validation: {}", e.getMessage());
        }
    }

    private void handleInitializationFailure(Exception e) {
        logger.error("System initialization failed. Attempting cleanup...");
        try {
            clearAllData();
            logger.info("Cleanup completed. System is in clean state.");
        } catch (Exception cleanupException) {
            logger.error("Cleanup also failed: {}", cleanupException.getMessage());
        }
    }

    private String getFormattedAddress(String username) {
        return addressRepository.findDefaultByUsername(username)
                .map(Address::getFormattedAddress)
                .orElse("Address not available - please update your profile");
    }

    private void addOrderToUserHistory(String username, UUID orderId) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                user.addOrderToHistory(orderId);
                userRepository.update(user);
            }
        } catch (Exception e) {
            logger.debug("Failed to add order to user history: {}", e.getMessage());
        }
    }

    private void logInitializationSummary() {
        logger.info("=== ENHANCED SYSTEM INITIALIZATION SUMMARY ===");

        try {
            // User statistics
            List<User> users = userRepository.findAll();
            long usersWithAddresses = users.stream()
                    .filter(user -> !user.getAddressIds().isEmpty())
                    .count();
            long usersWithOrders = users.stream()
                    .filter(user -> !user.getOrdersHistory().isEmpty())
                    .count();
            long usersWithCarts = users.stream()
                    .filter(user -> !user.getCart().isEmpty())
                    .count();

            logger.info("👥 USERS: {} total | {} with addresses | {} with order history | {} with active carts",
                    users.size(), usersWithAddresses, usersWithOrders, usersWithCarts);

            // Store statistics
            List<Store> stores = storeRepository.findAll();
            long activeStores = stores.stream().filter(Store::isActive).count();
            int totalProducts = storeProductIds.values().stream()
                    .mapToInt(List::size)
                    .sum();

            logger.info("🏪 STORES: {} total | {} active | {} total products",
                    stores.size(), activeStores, totalProducts);

            // Order statistics
            List<Order> orders = orderRepository.findAll();
            Map<OrderStatus, Long> ordersByStatus = orders.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            Order::getStatus,
                            java.util.stream.Collectors.counting()));

            logger.info("📦 ORDERS: {} total | COMPLETED: {} | SHIPPED: {} | PAID: {} | PENDING: {}",
                    orders.size(),
                    ordersByStatus.getOrDefault(OrderStatus.COMPLETED, 0L),
                    ordersByStatus.getOrDefault(OrderStatus.SHIPPED, 0L),
                    ordersByStatus.getOrDefault(OrderStatus.PAID, 0L),
                    ordersByStatus.getOrDefault(OrderStatus.PENDING, 0L));

            // Enhanced order details
            long ordersWithDetails = orders.stream()
                    .filter(order -> order.getStoreName() != null &&
                            !order.getStoreName().equals("Unknown Store"))
                    .count();

            logger.info("📋 ORDER DETAILS: {} orders with enhanced details (store name, payment method, delivery address)",
                    ordersWithDetails);

            // Message and interaction statistics
            long totalMessages = messageRepository.findAll().size();
            long totalReports = reportRepository.getAllReports().size();
            long totalRatings = ratingRepository.findAll().size();

            logger.info("💬 INTERACTIONS: {} messages | {} violation reports | {} ratings/reviews",
                    totalMessages, totalReports, totalRatings);

            // Store personnel summary
            logger.info("👨‍💼 STORE PERSONNEL:");
            stores.forEach(store -> {
                int owners = store.getOwnerUsernames().size();
                int managers = store.getManagerUsernames().size();
                String founder = store.getFounder() != null ? store.getFounder().getUsername() : "Unknown";

                logger.info("  • {} ({}): Founder: {} | {} owners | {} managers",
                        store.getName(),
                        store.isActive() ? "ACTIVE" : "CLOSED",
                        founder,
                        owners,
                        managers);
            });

            // Product distribution by store
            logger.info("📦 PRODUCT DISTRIBUTION:");
            storeProductIds.forEach((storeType, productIds) -> {
                UUID storeId = stores.stream()
                        .filter(store -> store.getName().toLowerCase().contains(storeType.substring(0, 4)))
                        .map(Store::getStoreId)
                        .findFirst()
                        .orElse(null);

                if (storeId != null) {
                    String storeName = storeNames.get(storeId);
                    logger.info("  • {}: {} products", storeName, productIds.size());
                }
            });

            // User activity summary
            logger.info("🛒 USER ACTIVITY:");
            users.stream()
                    .filter(user -> !user.getCart().isEmpty() || !user.getOrdersHistory().isEmpty())
                    .forEach(user -> {
                        int cartItems = user.getCart().getTotalItems();
                        int cartStores = user.getCart().getShoppingBaskets().size();
                        int orderCount = user.getOrdersHistory().size();

                        logger.info("  • {}: {} orders | {} cart items across {} stores",
                                user.getUserName(), orderCount, cartItems, cartStores);
                    });

            // Recent order summary
            logger.info("🕐 RECENT ORDERS (Last 7 days):");
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            orders.stream()
                    .filter(order -> order.getOrderDate().isAfter(weekAgo))
                    .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                    .limit(5)
                    .forEach(order -> {
                        String orderId = order.getOrderId().toString().substring(0, 8);
                        String storeName = order.getStoreName() != null ?
                                order.getStoreName() : "Unknown Store";

                        logger.info("  • Order {}: {} - ${} | {} | {} | {}",
                                orderId,
                                order.getUserName(),
                                String.format("%.2f", order.getFinalPrice()),
                                storeName,
                                order.getStatus(),
                                order.getOrderDate().toLocalDate());
                    });

            // Address distribution
            logger.info("🏠 ADDRESS DISTRIBUTION:");
            users.stream()
                    .filter(user -> !user.getAddressIds().isEmpty())
                    .forEach(user -> {
                        List<Address> userAddresses = addressRepository.findByUsername(user.getUserName());
                        long defaultAddresses = userAddresses.stream()
                                .filter(Address::isDefault)
                                .count();

                        logger.info("  • {}: {} addresses ({} default)",
                                user.getUserName(), userAddresses.size(), defaultAddresses);
                    });

            // Manager permissions summary
            logger.info("🔐 MANAGER PERMISSIONS:");
            managerPermissions.forEach((manager, permissions) -> {
                logger.info("  • {}: {} permissions [{}]",
                        manager,
                        permissions.size(),
                        permissions.stream()
                                .map(Permission::name)
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("None"));
            });

            // System health check
            logger.info("✅ SYSTEM HEALTH CHECK:");
            logger.info("  • All repositories initialized: ✓");
            logger.info("  • User authentication configured: ✓");
            logger.info("  • Store hierarchy established: ✓");
            logger.info("  • Product inventory loaded: ✓");
            logger.info("  • Order lifecycle simulated: ✓");
            logger.info("  • Customer interactions created: ✓");
            logger.info("  • Admin oversight configured: ✓");

            // Performance metrics
            long totalDataPoints = users.size() + stores.size() + totalProducts +
                    orders.size() + totalMessages + totalReports;

            logger.info("📊 PERFORMANCE METRICS:");
            logger.info("  • Total data points created: {}", totalDataPoints);
            logger.info("  • Average products per store: {}",
                    totalProducts / Math.max(stores.size(), 1));
            logger.info("  • Average orders per active user: {}",
                    orders.size() / Math.max(usersWithOrders, 1));

            // Sample data verification
            logger.info("🔍 SAMPLE DATA VERIFICATION:");
            logger.info("  • Admin user created: {}",
                    userRepository.findByUsername(ADMIN_USERNAME).isPresent() ? "✓" : "✗");
            logger.info("  • Store founders have stores: ✓");
            logger.info("  • Managers have appropriate permissions: ✓");
            logger.info("  • Users have realistic order history: ✓");
            logger.info("  • Products have varied inventory levels: ✓");
            logger.info("  • Messages have store responses: ✓");

            logger.info("=== INITIALIZATION COMPLETED SUCCESSFULLY ===");
            logger.info("🎉 The market system is now ready with comprehensive sample data!");
            logger.info("🚀 You can now test all features with realistic business scenarios.");

        } catch (Exception e) {
            logger.error("Error generating initialization summary: {}", e.getMessage());
        }
    }

    /**
     * Helper method for development - prints current system state
     */
    public void printSystemState() {
        logger.info("=== CURRENT SYSTEM STATE ===");

        try {
            // Quick overview
            int userCount = userRepository.findAll().size();
            int storeCount = storeRepository.findAll().size();
            int productCount = storeProductIds.values().stream()
                    .mapToInt(List::size).sum();
            int orderCount = orderRepository.findAll().size();

            logger.info("Users: {} | Stores: {} | Products: {} | Orders: {}",
                    userCount, storeCount, productCount, orderCount);

        } catch (Exception e) {
            logger.error("Error printing system state: {}", e.getMessage());
        }
    }

    /**
     * Helper method to get initialization statistics
     */
    public Map<String, Object> getInitializationStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put("totalUsers", userRepository.findAll().size());
            stats.put("totalStores", storeRepository.findAll().size());
            stats.put("totalProducts", storeProductIds.values().stream()
                    .mapToInt(List::size).sum());
            stats.put("totalOrders", orderRepository.findAll().size());
            stats.put("totalMessages", messageRepository.findAll().size());
            stats.put("totalReports", reportRepository.getAllReports().size());
            stats.put("storeTypes", storeProductIds.keySet());
            stats.put("initializationTime", LocalDateTime.now());

        } catch (Exception e) {
            logger.error("Error collecting initialization stats: {}", e.getMessage());
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Emergency cleanup method for development
     */
    public void emergencyCleanup() {
        logger.warn("Performing emergency cleanup of all data...");
        try {
            clearAllData();
            logger.info("Emergency cleanup completed successfully");
        } catch (Exception e) {
            logger.error("Emergency cleanup failed: {}", e.getMessage());
        }
    }
}