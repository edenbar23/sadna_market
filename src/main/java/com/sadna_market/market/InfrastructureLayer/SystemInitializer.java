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

import java.time.LocalDateTime;
import java.util.*;

/**
 * Enhanced system initializer that creates comprehensive sample data including:
 * - Users with addresses
 * - Stores with products and inventory
 * - Store personnel (owners, managers)
 * - Completed orders with transaction history, store names, payment methods, and delivery addresses
 * - User carts with items
 * - Ratings, reviews, and messages
 * - Reports and various states
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
    private final UserAccessService userAccessService;
    private final StoreManagementService storeManagementService;
    private final InventoryManagementService inventoryManagementService;
    private final RatingService ratingService;

    // Store product IDs for cross-referencing
    private final Map<String, List<UUID>> storeProductIds = new HashMap<>();

    // Store information for enhanced order creation
    private final Map<UUID, String> storeNames = new HashMap<>();

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
        this.userAccessService = userAccessService;
        this.storeManagementService = storeManagementService;
        this.inventoryManagementService = inventoryManagementService;
        this.ratingService = ratingService;
    }

    @Override
    public void run(String... args) {
        logger.info("Initializing system with comprehensive sample data...");

        try {
            // Clear existing data
            clearAllData();

            // Initialize users
            initializeUsers();

            // Initialize user addresses
            initializeUserAddresses();

            // Initialize stores
            Map<String, UUID> storeIds = initializeStores();

            // Initialize products
            initializeProducts(storeIds);

            // Initialize store personnel (owners and managers)
            initializeStorePersonnel(storeIds);

            // Initialize completed orders with enhanced details (store names, payment methods, delivery addresses)
            initializeCompletedOrders(storeIds);

            // Initialize user carts with items
            initializeUserCarts(storeIds);

            // Initialize ratings and reviews (after orders so users have purchase history)
            initializeRatingsAndReviews(storeIds);

            // Initialize messages and conversations
            initializeMessages(storeIds);

            // Initialize violation reports
            initializeReports(storeIds);

            logger.info("System successfully initialized with comprehensive sample data");
            logInitializationSummary();
        } catch (Exception e) {
            logger.error("Error initializing system with sample data: {}", e.getMessage(), e);
        }
    }

    private void clearAllData() {
        logger.info("Clearing existing data...");
        userRepository.clear();
        storeRepository.clear();
        productRepository.clear();
        orderRepository.clear();
        messageRepository.clear();
        reportRepository.clear();
        ratingRepository.clear();
        addressRepository.clear();
        authRepository.clear();
        storeNames.clear();
    }

    private void initializeUsers() {
        logger.info("Initializing users with Pokémon names...");

        // Create admin user
        User admin = new User("admin", "Admin123!", "admin@market.com", "System", "Admin");
        userRepository.save(admin);
        authRepository.addUser("admin", "Admin123!");

        // Create regular users with Pokémon names and varied data
        String[][] userData = {
                // username, password, email, firstName, lastName
                {"pikachu", "Password1!", "pikachu@example.com", "Pika", "Chu"},
                {"charizard", "Password1!", "charizard@example.com", "Chari", "Zard"},
                {"bulbasaur", "Password1!", "bulbasaur@example.com", "Bulba", "Saur"},
                {"squirtle", "Password1!", "squirtle@example.com", "Squir", "Tle"},
                {"mewtwo", "Password1!", "mewtwo@example.com", "Mew", "Two"},
                {"eevee", "Password1!", "eevee@example.com", "Ee", "Vee"},
                {"lucario", "Password1!", "lucario@example.com", "Luca", "Rio"},
                {"dragonite", "Password1!", "dragonite@example.com", "Dragon", "Ite"}
        };

        for (String[] user : userData) {
            userAccessService.registerUser(user[0], user[1], user[2], user[3], user[4]);
            authRepository.addUser(user[0], user[1]);
        }
    }

    private void initializeUserAddresses() {
        logger.info("Initializing user addresses...");

        // Addresses for pikachu (store owner)
        Address pikachuHome = new Address(
                "pikachu",
                "Pika Chu",
                "123 Thunderbolt Ave",
                "Apartment 25",
                "Viridian City",
                "Kanto",
                "12345",
                "Pokémon World",
                "555-PIKA-CHU",
                "Home"
        );
        pikachuHome.setDefault(true);
        addressRepository.save(pikachuHome);

        Address pikachuWork = new Address(
                "pikachu",
                "Pika Chu",
                "456 Electric Street",
                "Electric Type Gadgets Store",
                "Viridian City",
                "Kanto",
                "12346",
                "Pokémon World",
                "555-STORE-01",
                "Work"
        );
        addressRepository.save(pikachuWork);

        // Addresses for charizard (store owner) - using simple constructor
        Address charizardHome = new Address(
                "charizard",
                "Chari Zard",
                "789 Flame Mountain Road",
                "Cinnabar Island",
                "Kanto",
                "67890",
                "Pokémon World"
        );
        charizardHome.setDefault(true);
        charizardHome.setPhoneNumber("555-CHAR-IZAR");
        charizardHome.setLabel("Home");
        addressRepository.save(charizardHome);

        // Addresses for bulbasaur (store owner)
        Address bulbasaurHome = new Address(
                "bulbasaur",
                "Bulba Saur",
                "321 Garden Lane",
                "Celadon City",
                "Kanto",
                "54321",
                "Pokémon World"
        );
        bulbasaurHome.setDefault(true);
        bulbasaurHome.setPhoneNumber("555-BULBA-SAUR");
        bulbasaurHome.setLabel("Home");
        addressRepository.save(bulbasaurHome);

        Address bulbasaurGarden = new Address(
                "bulbasaur",
                "Bulba Saur",
                "999 Greenhouse Road",
                "Store Garden",
                "Celadon City",
                "Kanto",
                "54322",
                "Pokémon World",
                "555-GARDEN-01",
                "Garden"
        );
        addressRepository.save(bulbasaurGarden);

        // Addresses for other users
        Address squirtleHome = new Address(
                "squirtle",
                "Squir Tle",
                "147 Water Way",
                "Cerulean City",
                "Kanto",
                "98765",
                "Pokémon World"
        );
        squirtleHome.setDefault(true);
        squirtleHome.setPhoneNumber("555-SQUI-RTLE");
        squirtleHome.setLabel("Home");
        addressRepository.save(squirtleHome);

        Address mewtwoHome = new Address(
                "mewtwo",
                "Mew Two",
                "777 Psychic Plaza",
                "Penthouse Suite",
                "Saffron City",
                "Kanto",
                "11111",
                "Pokémon World",
                "555-MEW-TWO",
                "Home"
        );
        mewtwoHome.setDefault(true);
        addressRepository.save(mewtwoHome);

        Address eeveeHome = new Address(
                "eevee",
                "Ee Vee",
                "888 Evolution Avenue",
                "Lavender Town",
                "Kanto",
                "22222",
                "Pokémon World"
        );
        eeveeHome.setDefault(true);
        eeveeHome.setPhoneNumber("555-EE-VEE");
        eeveeHome.setLabel("Home");
        addressRepository.save(eeveeHome);

        // Add addresses for lucario and dragonite
        Address lucarioHome = new Address(
                "lucario",
                "Luca Rio",
                "456 Aura Street",
                "Fighting Dojo",
                "Canalave City",
                "Sinnoh",
                "33333",
                "Pokémon World",
                "555-LUCA-RIO",
                "Home"
        );
        lucarioHome.setDefault(true);
        addressRepository.save(lucarioHome);

        Address dragoniteHome = new Address(
                "dragonite",
                "Dragon Ite",
                "999 Sky Highway",
                "Dragon's Den",
                "Blackthorn City",
                "Johto",
                "44444",
                "Pokémon World",
                "555-DRAG-ONITE",
                "Home"
        );
        dragoniteHome.setDefault(true);
        addressRepository.save(dragoniteHome);

        // Add address IDs to users
        User pikachu = userRepository.findByUsername("pikachu").orElse(null);
        if (pikachu != null) {
            pikachu.addAddressId(pikachuHome.getAddressId());
            pikachu.addAddressId(pikachuWork.getAddressId());
            userRepository.update(pikachu);
        }

        User charizard = userRepository.findByUsername("charizard").orElse(null);
        if (charizard != null) {
            charizard.addAddressId(charizardHome.getAddressId());
            userRepository.update(charizard);
        }

        User bulbasaur = userRepository.findByUsername("bulbasaur").orElse(null);
        if (bulbasaur != null) {
            bulbasaur.addAddressId(bulbasaurHome.getAddressId());
            bulbasaur.addAddressId(bulbasaurGarden.getAddressId());
            userRepository.update(bulbasaur);
        }

        User squirtle = userRepository.findByUsername("squirtle").orElse(null);
        if (squirtle != null) {
            squirtle.addAddressId(squirtleHome.getAddressId());
            userRepository.update(squirtle);
        }

        User mewtwo = userRepository.findByUsername("mewtwo").orElse(null);
        if (mewtwo != null) {
            mewtwo.addAddressId(mewtwoHome.getAddressId());
            userRepository.update(mewtwo);
        }

        User eevee = userRepository.findByUsername("eevee").orElse(null);
        if (eevee != null) {
            eevee.addAddressId(eeveeHome.getAddressId());
            userRepository.update(eevee);
        }

        User lucario = userRepository.findByUsername("lucario").orElse(null);
        if (lucario != null) {
            lucario.addAddressId(lucarioHome.getAddressId());
            userRepository.update(lucario);
        }

        User dragonite = userRepository.findByUsername("dragonite").orElse(null);
        if (dragonite != null) {
            dragonite.addAddressId(dragoniteHome.getAddressId());
            userRepository.update(dragonite);
        }
    }

    private Map<String, UUID> initializeStores() {
        logger.info("Initializing stores...");
        Map<String, UUID> storeIds = new HashMap<>();

        // Store 1: Electronics Store (Owner: pikachu)
        Store electronicsStore = storeManagementService.createStore(
                "pikachu",
                "Electric Type Gadgets",
                "Shocking deals on all electronics! From smartphones to gaming consoles, we've got the power you need.",
                "123 Thunderbolt Ave, Viridian City, Kanto 12345",
                "contact@electrictype.com",
                "555-PIKA-CHU"
        );
        storeIds.put("electronics", electronicsStore.getStoreId());
        storeNames.put(electronicsStore.getStoreId(), "Electric Type Gadgets");

        // Store 2: Clothing Store (Owner: charizard)
        Store clothingStore = storeManagementService.createStore(
                "charizard",
                "Fire Fashion",
                "Hot styles that will burn up the competition! Fashion that's always blazing with style.",
                "456 Flame St, Cinnabar Island, Kanto 67890",
                "info@firefashion.com",
                "555-CHAR-IZAR"
        );
        storeIds.put("clothing", clothingStore.getStoreId());
        storeNames.put(clothingStore.getStoreId(), "Fire Fashion");

        // Store 3: Grocery Store (Owner: bulbasaur)
        Store groceryStore = storeManagementService.createStore(
                "bulbasaur",
                "Grass Grocers",
                "Fresh organic produce grown with solar energy. Naturally healthy food for a better tomorrow.",
                "789 Vine Whip Road, Celadon City, Kanto 54321",
                "orders@grassgrocers.com",
                "555-BULBA-SAUR"
        );
        storeIds.put("grocery", groceryStore.getStoreId());
        storeNames.put(groceryStore.getStoreId(), "Grass Grocers");

        // Store 4: Bookstore (Owner: mewtwo)
        Store bookStore = storeManagementService.createStore(
                "mewtwo",
                "Psychic Knowledge",
                "Expand your mind with our vast collection of books and educational materials.",
                "777 Psychic Plaza, Saffron City, Kanto 11111",
                "books@psychicknowledge.com",
                "555-MEW-TWO"
        );
        storeIds.put("books", bookStore.getStoreId());
        storeNames.put(bookStore.getStoreId(), "Psychic Knowledge");

        return storeIds;
    }

    private void initializeProducts(Map<String, UUID> storeIds) {
        logger.info("Initializing products...");

        // Electronics store products
        UUID electronicsStoreId = storeIds.get("electronics");
        List<UUID> electronicsProductIds = new ArrayList<>();

        Object[][] electronicsProducts = {
                // name, category, description, price, quantity
                {"Thunder Phone Pro", "Smartphones", "Latest smartphone with lightning-fast charging and electric-type compatibility", 899.99, 15},
                {"Volt Laptop Gaming", "Computers", "High-performance gaming laptop powered by Jolteon technology", 1299.99, 8},
                {"Zaptos Headphones", "Audio", "Noise-cancelling headphones with thunderous bass", 199.99, 20},
                {"Spark TV 55\"", "TVs", "4K OLED TV with lightning-fast refresh rate and HDR support", 699.99, 12},
                {"Pikachu Gaming Console", "Gaming", "Next-gen gaming console with electric-type exclusives", 499.99, 10},
                {"Electric Charger Pro", "Accessories", "Universal fast charger for all electric-type devices", 49.99, 30},
                {"Thunder Tablet", "Tablets", "High-resolution tablet perfect for digital art and productivity", 399.99, 18}
        };

        for (Object[] product : electronicsProducts) {
            UUID productId = inventoryManagementService.addProductToStore(
                    "pikachu",
                    electronicsStoreId,
                    (String) product[0],
                    (String) product[1],
                    (String) product[2],
                    (double) product[3],
                    (int) product[4]
            );
            electronicsProductIds.add(productId);
        }
        storeProductIds.put("electronics", electronicsProductIds);

        // Clothing store products
        UUID clothingStoreId = storeIds.get("clothing");
        List<UUID> clothingProductIds = new ArrayList<>();

        Object[][] clothingProducts = {
                // name, category, description, price, quantity
                {"Blaze T-Shirt Premium", "Men", "Fire-resistant premium cotton t-shirt with flame patterns", 29.99, 25},
                {"Inferno Summer Dress", "Women", "Elegant summer dress that's always in season, perfect for hot weather", 69.99, 18},
                {"Ember Kids Shorts", "Children", "Comfortable shorts that keep little ones warm and stylish", 24.99, 30},
                {"Flame Denim Jeans", "Men", "Stylish denim jeans with subtle flame embroidery", 79.99, 22},
                {"Heat Wave Baseball Cap", "Accessories", "Trendy cap that keeps you cool while looking hot", 34.99, 40},
                {"Phoenix Leather Jacket", "Women", "Premium leather jacket with fire-type styling", 199.99, 8},
                {"Charmander Hoodie", "Children", "Cozy hoodie featuring the beloved fire starter Pokémon", 49.99, 15}
        };

        for (Object[] product : clothingProducts) {
            UUID productId = inventoryManagementService.addProductToStore(
                    "charizard",
                    clothingStoreId,
                    (String) product[0],
                    (String) product[1],
                    (String) product[2],
                    (double) product[3],
                    (int) product[4]
            );
            clothingProductIds.add(productId);
        }
        storeProductIds.put("clothing", clothingProductIds);

        // Grocery store products
        UUID groceryStoreId = storeIds.get("grocery");
        List<UUID> groceryProductIds = new ArrayList<>();

        Object[][] groceryProducts = {
                // name, category, description, price, quantity
                {"Pecha Berries Organic", "Fruits", "Naturally sweet berries that cure poisoning, organically grown", 4.99, 50},
                {"Solar Whole Grain Bread", "Bakery", "Nutritious bread baked using only clean solar energy", 5.99, 35},
                {"Free-Range Chansey Eggs", "Dairy", "Premium eggs from happy free-range Chansey", 7.99, 45},
                {"Grass-Fed Tauros Steak", "Meat", "Prime cut steak from ethically raised Tauros", 12.99, 25},
                {"Oddish Organic Spinach", "Vegetables", "Fresh spinach picked from our sustainable garden", 3.99, 40},
                {"Sitrus Berry Juice", "Beverages", "Refreshing juice that restores energy naturally", 6.99, 30},
                {"Leppa Berry Trail Mix", "Snacks", "Healthy trail mix with energy-restoring berries", 8.99, 28}
        };

        for (Object[] product : groceryProducts) {
            UUID productId = inventoryManagementService.addProductToStore(
                    "bulbasaur",
                    groceryStoreId,
                    (String) product[0],
                    (String) product[1],
                    (String) product[2],
                    (double) product[3],
                    (int) product[4]
            );
            groceryProductIds.add(productId);
        }
        storeProductIds.put("grocery", groceryProductIds);

        // Bookstore products
        UUID bookStoreId = storeIds.get("books");
        List<UUID> bookProductIds = new ArrayList<>();

        Object[][] bookProducts = {
                // name, category, description, price, quantity
                {"Pokémon Encyclopedia", "Reference", "Complete guide to all known Pokémon species", 39.99, 20},
                {"Psychic Powers Handbook", "Self-Help", "Learn to harness your inner psychic abilities", 24.99, 15},
                {"Battle Strategy Guide", "Strategy", "Advanced tactics for Pokémon battles", 19.99, 25},
                {"Meditation for Trainers", "Wellness", "Find inner peace and strengthen your bond with Pokémon", 16.99, 18},
                {"Type Effectiveness Charts", "Educational", "Visual guide to Pokémon type matchups", 12.99, 30},
                {"Legendary Tales", "Fiction", "Epic stories of legendary Pokémon encounters", 22.99, 12}
        };

        for (Object[] product : bookProducts) {
            UUID productId = inventoryManagementService.addProductToStore(
                    "mewtwo",
                    bookStoreId,
                    (String) product[0],
                    (String) product[1],
                    (String) product[2],
                    (double) product[3],
                    (int) product[4]
            );
            bookProductIds.add(productId);
        }
        storeProductIds.put("books", bookProductIds);
    }

    private void initializeStorePersonnel(Map<String, UUID> storeIds) {
        logger.info("Initializing store personnel...");

        // Electronics store: squirtle as manager
        UUID electronicsStoreId = storeIds.get("electronics");
        Set<Permission> electronicsManagerPermissions = new HashSet<>(Arrays.asList(
                Permission.MANAGE_INVENTORY,
                Permission.ADD_PRODUCT,
                Permission.REMOVE_PRODUCT,
                Permission.UPDATE_PRODUCT,
                Permission.RESPOND_TO_USER_INQUIRIES,
                Permission.VIEW_STORE_PURCHASE_HISTORY
        ));
        storeManagementService.appointStoreManager("pikachu", electronicsStoreId, "squirtle", electronicsManagerPermissions);

        // Clothing store: mewtwo as additional owner, eevee as manager
        UUID clothingStoreId = storeIds.get("clothing");
        storeManagementService.appointStoreOwner("charizard", clothingStoreId, "mewtwo");

        Set<Permission> clothingManagerPermissions = new HashSet<>(Arrays.asList(
                Permission.MANAGE_INVENTORY,
                Permission.RESPOND_TO_USER_INQUIRIES,
                Permission.VIEW_STORE_PURCHASE_HISTORY,
                Permission.UPDATE_PRODUCT
        ));
        storeManagementService.appointStoreManager("charizard", clothingStoreId, "eevee", clothingManagerPermissions);

        // Grocery store: squirtle as manager here too
        UUID groceryStoreId = storeIds.get("grocery");
        Set<Permission> groceryManagerPermissions = new HashSet<>(Arrays.asList(
                Permission.MANAGE_INVENTORY,
                Permission.RESPOND_TO_USER_INQUIRIES,
                Permission.VIEW_STORE_PURCHASE_HISTORY,
                Permission.ADD_PRODUCT
        ));
        storeManagementService.appointStoreManager("bulbasaur", groceryStoreId, "squirtle", groceryManagerPermissions);

        // Bookstore: lucario as manager
        UUID bookStoreId = storeIds.get("books");
        Set<Permission> bookManagerPermissions = new HashSet<>(Arrays.asList(
                Permission.MANAGE_INVENTORY,
                Permission.ADD_PRODUCT,
                Permission.REMOVE_PRODUCT,
                Permission.UPDATE_PRODUCT,
                Permission.RESPOND_TO_USER_INQUIRIES
        ));
        storeManagementService.appointStoreManager("mewtwo", bookStoreId, "lucario", bookManagerPermissions);
    }

    private void initializeCompletedOrders(Map<String, UUID> storeIds) {
        logger.info("Initializing completed orders with enhanced details (store names, payment methods, delivery addresses)...");

        LocalDateTime baseTime = LocalDateTime.now().minusDays(30);

        // Order 1: eevee buys electronics
        List<UUID> electronicsProducts = storeProductIds.get("electronics");
        if (!electronicsProducts.isEmpty()) {
            Map<UUID, Integer> order1Products = new HashMap<>();
            order1Products.put(electronicsProducts.get(0), 1); // Thunder Phone Pro
            order1Products.put(electronicsProducts.get(2), 1); // Zaptos Headphones

            UUID order1Id = orderRepository.createOrderWithDetails(
                    storeIds.get("electronics"),
                    "eevee",
                    order1Products,
                    1099.98, // Total price
                    1099.98, // Final price (no discount)
                    baseTime.plusDays(1),
                    OrderStatus.COMPLETED,
                    10001, // Transaction ID
                    storeNames.get(storeIds.get("electronics")), // Store name
                    "Credit Card (**** 4567)", // Payment method
                    getDeliveryAddress("eevee") // Delivery address
            );

            // Add to user's order history
            User eevee = userRepository.findByUsername("eevee").orElse(null);
            if (eevee != null) {
                eevee.addOrderToHistory(order1Id);
                userRepository.update(eevee);
            }
        }

        // Order 2: lucario buys clothing
        List<UUID> clothingProducts = storeProductIds.get("clothing");
        if (!clothingProducts.isEmpty()) {
            Map<UUID, Integer> order2Products = new HashMap<>();
            order2Products.put(clothingProducts.get(0), 2); // Blaze T-Shirt Premium
            order2Products.put(clothingProducts.get(4), 1); // Heat Wave Baseball Cap

            UUID order2Id = orderRepository.createOrderWithDetails(
                    storeIds.get("clothing"),
                    "lucario",
                    order2Products,
                    94.97, // Total price
                    94.97, // Final price
                    baseTime.plusDays(3),
                    OrderStatus.COMPLETED,
                    10002,
                    storeNames.get(storeIds.get("clothing")),
                    "Credit Card (**** 8901)",
                    getDeliveryAddress("lucario")
            );

            User lucario = userRepository.findByUsername("lucario").orElse(null);
            if (lucario != null) {
                lucario.addOrderToHistory(order2Id);
                userRepository.update(lucario);
            }
        }

        // Order 3: dragonite buys groceries
        List<UUID> groceryProducts = storeProductIds.get("grocery");
        if (!groceryProducts.isEmpty()) {
            Map<UUID, Integer> order3Products = new HashMap<>();
            order3Products.put(groceryProducts.get(0), 3); // Pecha Berries
            order3Products.put(groceryProducts.get(1), 2); // Solar Bread
            order3Products.put(groceryProducts.get(5), 1); // Sitrus Berry Juice

            UUID order3Id = orderRepository.createOrderWithDetails(
                    storeIds.get("grocery"),
                    "dragonite",
                    order3Products,
                    33.95, // Total price
                    33.95, // Final price
                    baseTime.plusDays(5),
                    OrderStatus.COMPLETED,
                    10003,
                    storeNames.get(storeIds.get("grocery")),
                    "Credit Card (**** 2345)",
                    getDeliveryAddress("dragonite")
            );

            User dragonite = userRepository.findByUsername("dragonite").orElse(null);
            if (dragonite != null) {
                dragonite.addOrderToHistory(order3Id);
                userRepository.update(dragonite);
            }
        }

        // Order 4: eevee buys books
        List<UUID> bookProducts = storeProductIds.get("books");
        if (!bookProducts.isEmpty()) {
            Map<UUID, Integer> order4Products = new HashMap<>();
            order4Products.put(bookProducts.get(0), 1); // Pokémon Encyclopedia
            order4Products.put(bookProducts.get(3), 1); // Meditation for Trainers

            UUID order4Id = orderRepository.createOrderWithDetails(
                    storeIds.get("books"),
                    "eevee",
                    order4Products,
                    56.98, // Total price
                    56.98, // Final price
                    baseTime.plusDays(7),
                    OrderStatus.COMPLETED,
                    10004,
                    storeNames.get(storeIds.get("books")),
                    "Credit Card (**** 4567)",
                    getDeliveryAddress("eevee")
            );

            User eevee = userRepository.findByUsername("eevee").orElse(null);
            if (eevee != null) {
                eevee.addOrderToHistory(order4Id);
                userRepository.update(eevee);
            }
        }

        // Order 5: mewtwo buys electronics (with different delivery address)
        if (!electronicsProducts.isEmpty()) {
            Map<UUID, Integer> order5Products = new HashMap<>();
            order5Products.put(electronicsProducts.get(1), 1); // Volt Laptop Gaming
            order5Products.put(electronicsProducts.get(6), 1); // Thunder Tablet

            UUID order5Id = orderRepository.createOrderWithDetails(
                    storeIds.get("electronics"),
                    "mewtwo",
                    order5Products,
                    1699.98, // Total price
                    1699.98, // Final price
                    baseTime.plusDays(10),
                    OrderStatus.COMPLETED,
                    10005,
                    storeNames.get(storeIds.get("electronics")),
                    "Credit Card (**** 9999)",
                    getDeliveryAddress("mewtwo")
            );

            User mewtwo = userRepository.findByUsername("mewtwo").orElse(null);
            if (mewtwo != null) {
                mewtwo.addOrderToHistory(order5Id);
                userRepository.update(mewtwo);
            }
        }

        // Order 6: squirtle buys clothing (shipped order)
        if (!clothingProducts.isEmpty()) {
            Map<UUID, Integer> order6Products = new HashMap<>();
            order6Products.put(clothingProducts.get(1), 1); // Inferno Summer Dress
            order6Products.put(clothingProducts.get(5), 1); // Phoenix Leather Jacket

            UUID order6Id = orderRepository.createOrderWithDetails(
                    storeIds.get("clothing"),
                    "squirtle",
                    order6Products,
                    269.98, // Total price
                    269.98, // Final price
                    baseTime.plusDays(12),
                    OrderStatus.SHIPPED,
                    10006,
                    storeNames.get(storeIds.get("clothing")),
                    "Credit Card (**** 7777)",
                    getDeliveryAddress("squirtle")
            );

            User squirtle = userRepository.findByUsername("squirtle").orElse(null);
            if (squirtle != null) {
                squirtle.addOrderToHistory(order6Id);
                userRepository.update(squirtle);
            }
        }

        // Recent pending order (no enhanced details yet since payment not processed)
        if (!electronicsProducts.isEmpty()) {
            Map<UUID, Integer> pendingOrderProducts = new HashMap<>();
            pendingOrderProducts.put(electronicsProducts.get(4), 1); // Gaming Console

            orderRepository.createOrder(
                    storeIds.get("electronics"),
                    "dragonite",
                    pendingOrderProducts,
                    499.99,
                    499.99,
                    LocalDateTime.now().minusHours(2),
                    OrderStatus.PENDING,
                    -1 // No transaction ID yet
            );
        }

        // Recent paid order (ready for shipping)
        if (!groceryProducts.isEmpty()) {
            Map<UUID, Integer> paidOrderProducts = new HashMap<>();
            paidOrderProducts.put(groceryProducts.get(3), 2); // Tauros Steak
            paidOrderProducts.put(groceryProducts.get(6), 1); // Trail Mix

            orderRepository.createOrderWithDetails(
                    storeIds.get("grocery"),
                    "lucario",
                    paidOrderProducts,
                    34.97, // Total price
                    34.97, // Final price
                    LocalDateTime.now().minusHours(6),
                    OrderStatus.PAID,
                    10007,
                    storeNames.get(storeIds.get("grocery")),
                    "Credit Card (**** 8901)",
                    getDeliveryAddress("lucario")
            );
        }
    }

    /**
     * Helper method to get user's delivery address for order initialization
     */
    private String getDeliveryAddress(String username) {
        Optional<Address> address = addressRepository.findDefaultByUsername(username);
        return address.map(Address::getFormattedAddress)
                .orElse("Address not found - please update your profile");
    }

    private void initializeUserCarts(Map<String, UUID> storeIds) {
        logger.info("Initializing user carts with items...");

        // Add items to mewtwo's cart
        List<UUID> electronicsProducts = storeProductIds.get("electronics");
        List<UUID> bookProducts = storeProductIds.get("books");

        if (!electronicsProducts.isEmpty() && !bookProducts.isEmpty()) {
            try {
                userAccessService.loginUser("mewtwo", "Password1!");
                userAccessService.addToCart("mewtwo", storeIds.get("electronics"), electronicsProducts.get(1), 1); // Laptop
                userAccessService.addToCart("mewtwo", storeIds.get("books"), bookProducts.get(1), 2); // Psychic Powers Handbook
                userAccessService.logoutUser("mewtwo");
            } catch (Exception e) {
                logger.warn("Could not initialize mewtwo's cart: {}", e.getMessage());
            }
        }

        // Add items to dragonite's cart
        List<UUID> clothingProducts = storeProductIds.get("clothing");
        List<UUID> groceryProducts = storeProductIds.get("grocery");

        if (!clothingProducts.isEmpty() && !groceryProducts.isEmpty()) {
            try {
                userAccessService.loginUser("dragonite", "Password1!");
                userAccessService.addToCart("dragonite", storeIds.get("clothing"), clothingProducts.get(5), 1); // Leather Jacket
                userAccessService.addToCart("dragonite", storeIds.get("grocery"), groceryProducts.get(3), 2); // Tauros Steak
                userAccessService.logoutUser("dragonite");
            } catch (Exception e) {
                logger.warn("Could not initialize dragonite's cart: {}", e.getMessage());
            }
        }

        // Add items to lucario's cart
        if (!electronicsProducts.isEmpty()) {
            try {
                userAccessService.loginUser("lucario", "Password1!");
                userAccessService.addToCart("lucario", storeIds.get("electronics"), electronicsProducts.get(6), 1); // Thunder Tablet
                userAccessService.logoutUser("lucario");
            } catch (Exception e) {
                logger.warn("Could not initialize lucario's cart: {}", e.getMessage());
            }
        }
    }

    private void initializeRatingsAndReviews(Map<String, UUID> storeIds) {
        logger.info("Initializing ratings and reviews based on purchase history...");

        // Since we have completed orders, users can now rate and review products they bought

        // eevee rates electronics products (from order 1)
        List<UUID> electronicsProducts = storeProductIds.get("electronics");
        if (!electronicsProducts.isEmpty()) {
            try {
                ratingService.rateProduct("eevee", electronicsProducts.get(0), 5); // Thunder Phone Pro
                ratingService.reviewProduct("eevee", electronicsProducts.get(0), storeIds.get("electronics"),
                        "Amazing phone! The electric-type compatibility is perfect for my evolution needs. Charges super fast!");

                ratingService.rateProduct("eevee", electronicsProducts.get(2), 4); // Zaptos Headphones
                ratingService.reviewProduct("eevee", electronicsProducts.get(2), storeIds.get("electronics"),
                        "Great sound quality! The thunderous bass really brings out the electric-type moves in games.");
            } catch (Exception e) {
                logger.warn("Could not create eevee's electronics ratings: {}", e.getMessage());
            }
        }

        // lucario rates clothing products (from order 2)
        List<UUID> clothingProducts = storeProductIds.get("clothing");
        if (!clothingProducts.isEmpty()) {
            try {
                ratingService.rateProduct("lucario", clothingProducts.get(0), 4); // Blaze T-Shirt Premium
                ratingService.reviewProduct("lucario", clothingProducts.get(0), storeIds.get("clothing"),
                        "High quality material! Perfect for training sessions. The fire-resistant fabric is actually useful.");

                ratingService.rateProduct("lucario", clothingProducts.get(4), 5); // Heat Wave Baseball Cap
                ratingService.reviewProduct("lucario", clothingProducts.get(4), storeIds.get("clothing"),
                        "Stylish and functional! Keeps the sun out during outdoor training. Highly recommend!");
            } catch (Exception e) {
                logger.warn("Could not create lucario's clothing ratings: {}", e.getMessage());
            }
        }

        // dragonite rates grocery products (from order 3)
        List<UUID> groceryProducts = storeProductIds.get("grocery");
        if (!groceryProducts.isEmpty()) {
            try {
                ratingService.rateProduct("dragonite", groceryProducts.get(0), 5); // Pecha Berries
                ratingService.reviewProduct("dragonite", groceryProducts.get(0), storeIds.get("grocery"),
                        "These berries are incredibly fresh and sweet! Perfect for a dragon's diet. Will definitely order again.");

                ratingService.rateProduct("dragonite", groceryProducts.get(1), 4); // Solar Bread
                ratingService.reviewProduct("dragonite", groceryProducts.get(1), storeIds.get("grocery"),
                        "Healthy and nutritious! You can really taste the solar energy. Great for pre-flight meals.");
            } catch (Exception e) {
                logger.warn("Could not create dragonite's grocery ratings: {}", e.getMessage());
            }
        }

        // eevee rates book products (from order 4)
        List<UUID> bookProducts = storeProductIds.get("books");
        if (!bookProducts.isEmpty()) {
            try {
                ratingService.rateProduct("eevee", bookProducts.get(0), 5); // Pokémon Encyclopedia
                ratingService.reviewProduct("eevee", bookProducts.get(0), storeIds.get("books"),
                        "Comprehensive and well-written! Helped me understand all my potential evolution paths. A must-have!");

                ratingService.rateProduct("eevee", bookProducts.get(3), 4); // Meditation for Trainers
                ratingService.reviewProduct("eevee", bookProducts.get(3), storeIds.get("books"),
                        "Peaceful and insightful. The meditation techniques really help with evolution preparation.");
            } catch (Exception e) {
                logger.warn("Could not create eevee's book ratings: {}", e.getMessage());
            }
        }

        // Add store ratings
        try {
            ratingService.rateStore("eevee", storeIds.get("electronics"), 5,
                    "Pikachu's store is fantastic! Great selection of electric-type compatible devices. Customer service is shocking!");

            ratingService.rateStore("lucario", storeIds.get("clothing"), 4,
                    "Charizard's fashion sense is fire! Quality clothing that can withstand intense training sessions.");

            ratingService.rateStore("dragonite", storeIds.get("grocery"), 5,
                    "Bulbasaur grows the best organic produce! Everything is so fresh and full of natural energy.");

            ratingService.rateStore("eevee", storeIds.get("books"), 5,
                    "Mewtwo's collection is incredible! Such deep knowledge and wisdom in every book. Psychically perfect!");
        } catch (Exception e) {
            logger.warn("Could not create store ratings: {}", e.getMessage());
        }
    }

    private void initializeMessages(Map<String, UUID> storeIds) {
        logger.info("Initializing messages and conversations...");

        // Message 1: Customer inquiry about electronics
        Message electronicsMessage1 = new Message(
                "dragonite",
                storeIds.get("electronics"),
                "Hi! I'm interested in the Thunder Phone Pro. Does it come with a Dragon-type case option? I tend to be a bit rough with my devices during flights."
        );
        messageRepository.save(electronicsMessage1);

        // Reply from store manager
        messageRepository.addReply(
                electronicsMessage1.getMessageId(),
                "squirtle", // The manager replies
                "Hello Dragonite! Unfortunately, we don't have Dragon-type cases yet, but our Thunder Phone Pro comes with a super durable electric-resistant case that should handle even the most turbulent flights. We also offer a 2-year protection plan!"
        );

        // Message 2: Customer asking about clothing sizes
        Message clothingMessage1 = new Message(
                "lucario",
                storeIds.get("clothing"),
                "Do you have the Phoenix Leather Jacket in size XL? I need something that fits well with my aura sensors."
        );
        messageRepository.save(clothingMessage1);

        messageRepository.addReply(
                clothingMessage1.getMessageId(),
                "eevee", // Manager replies
                "Hi Lucario! Yes, we have the Phoenix Leather Jacket in XL. It's specifically designed with aura-conductive materials that won't interfere with psychic or aura abilities. Would you like me to hold one for you?"
        );

        // Message 3: Grocery inquiry
        Message groceryMessage1 = new Message(
                "mewtwo",
                storeIds.get("grocery"),
                "I'm looking for brain food that enhances psychic abilities. Do you have any recommendations from your organic selection?"
        );
        messageRepository.save(groceryMessage1);

        messageRepository.addReply(
                groceryMessage1.getMessageId(),
                "squirtle", // Manager at grocery store
                "Greetings Mewtwo! I recommend our Sitrus Berry juice and Leppa Berry trail mix. Both are known to enhance mental clarity and psychic energy. Our Pecha Berries are also great for meditation. All grown with solar energy for maximum potency!"
        );

        // Message 4: Book store question
        Message bookMessage1 = new Message(
                "eevee",
                storeIds.get("books"),
                "Hi! I just finished reading the Pokémon Encyclopedia and loved it. Do you have any advanced evolution guides or specialized books for multi-evolution Pokémon like myself?"
        );
        messageRepository.save(bookMessage1);

        messageRepository.addReply(
                bookMessage1.getMessageId(),
                "lucario", // Book store manager
                "Hello Eevee! Great to hear you enjoyed the encyclopedia. I have a special order arriving next week: 'The Evolution Mastery Guide' specifically written for Pokémon with multiple evolution paths. It covers environmental triggers, stone compatibility, and emotional evolution methods. Would you like me to reserve a copy?"
        );

        // Message 5: Unread message
        Message unreadMessage = new Message(
                "dragonite",
                storeIds.get("clothing"),
                "I heard you have fire-resistant clothing. Do you have anything that's also wind-resistant for high-altitude flying?"
        );
        messageRepository.save(unreadMessage);

        // Message 6: Another unread message
        Message unreadMessage2 = new Message(
                "lucario",
                storeIds.get("electronics"),
                "Are your gaming consoles compatible with aura-based control systems? I prefer not to use traditional controllers."
        );
        messageRepository.save(unreadMessage2);
    }

    private void initializeReports(Map<String, UUID> storeIds) {
        logger.info("Initializing violation reports...");

        List<UUID> electronicsProducts = storeProductIds.get("electronics");
        List<UUID> clothingProducts = storeProductIds.get("clothing");

        if (!electronicsProducts.isEmpty()) {
            // Report 1: Product quality issue
            try {
                userAccessService.loginUser("dragonite", "Password1!");
                userAccessService.reportViolation(
                        "dragonite",
                        storeIds.get("electronics"),
                        electronicsProducts.get(1), // Volt Laptop Gaming
                        "The laptop overheated during a simple gaming session. This seems like a safety hazard, especially for fire-type Pokémon users."
                );
                userAccessService.logoutUser("dragonite");
            } catch (Exception e) {
                logger.warn("Could not create dragonite's report: {}", e.getMessage());
            }
        }

        if (!clothingProducts.isEmpty()) {
            // Report 2: False advertising
            Report report2 = new Report(
                    "lucario",
                    "The 'fire-resistant' t-shirt I bought got singed during a mild training session with a Charmander. The product description seems misleading.",
                    storeIds.get("clothing"),
                    clothingProducts.get(0) // Blaze T-Shirt Premium
            );
            reportRepository.save(report2);

            User lucario = userRepository.findByUsername("lucario").orElse(null);
            if (lucario != null) {
                lucario.addReport(report2.getReportId());
                userRepository.update(lucario);
            }
        }
    }

    private void logInitializationSummary() {
        logger.info("=== SYSTEM INITIALIZATION SUMMARY ===");

        // Count users
        List<User> users = userRepository.findAll();
        long loggedInUsers = users.stream().filter(User::isLoggedIn).count();
        logger.info("Users: {} total, {} currently logged in", users.size(), loggedInUsers);

        // Count addresses
        long totalAddresses = users.stream()
                .mapToLong(user -> addressRepository.findByUsername(user.getUserName()).size())
                .sum();
        logger.info("Addresses: {} total across all users", totalAddresses);

        // Count stores
        List<Store> stores = storeRepository.findAll();
        long activeStores = stores.stream().filter(Store::isActive).count();
        logger.info("Stores: {} total, {} active", stores.size(), activeStores);

        // Count products
        List<Optional<Product>> products = productRepository.findAll();
        long availableProducts = products.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(Product::isAvailable)
                .count();
        logger.info("Products: {} total, {} available", products.size(), availableProducts);

        // Count orders with enhanced details
        List<Order> orders = orderRepository.findAll();
        long completedOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .count();
        long ordersWithDetails = orders.stream()
                .filter(order -> order.getStoreName() != null && !order.getStoreName().equals("Unknown Store"))
                .count();
        logger.info("Orders: {} total, {} completed, {} with enhanced details (store name, payment method, delivery address)",
                orders.size(), completedOrders, ordersWithDetails);

        // Count reports
        long totalReports = reportRepository.getAllReports().size();
        logger.info("Reports: {} violation reports", totalReports);

        // Count user carts with items
        long usersWithCartItems = users.stream()
                .filter(user -> !user.getCart().isEmpty())
                .count();
        logger.info("Active Carts: {} users have items in their carts", usersWithCartItems);

        logger.info("=== INITIALIZATION COMPLETE ===");

        // Log some sample data for verification
        logger.info("Sample data verification:");
        logger.info("- pikachu owns 'Electric Type Gadgets' store");
        logger.info("- eevee has completed {} orders", orderRepository.findByUserName("eevee").size());
        logger.info("- Electronics store has {} products", storeProductIds.get("electronics").size());
        logger.info("- Users with addresses: {}",
                users.stream()
                        .filter(user -> !user.getAddressIds().isEmpty())
                        .map(User::getUserName)
                        .toArray());

        // Log store personnel summary
        logger.info("Store Personnel Summary:");
        stores.forEach(store -> {
            logger.info("- {} ({}): {} owners, {} managers",
                    store.getName(),
                    store.isActive() ? "ACTIVE" : "CLOSED",
                    store.getOwnerUsernames().size(),
                    store.getManagerUsernames().size());
        });

        // Log user cart summary
        logger.info("User Cart Summary:");
        users.stream()
                .filter(user -> !user.getCart().isEmpty())
                .forEach(user -> {
                    int totalItems = user.getCart().getTotalItems();
                    int totalStores = user.getCart().getShoppingBaskets().size();
                    logger.info("- {}: {} items across {} stores",
                            user.getUserName(), totalItems, totalStores);
                });

        // Enhanced order details summary
        logger.info("Enhanced Order Details Summary:");
        orders.stream()
                .filter(order -> order.getStoreName() != null && !order.getStoreName().equals("Unknown Store"))
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .limit(5)
                .forEach(order -> {
                    logger.info("- Order {}: {} - ${} ({}) | Store: {} | Payment: {} | Status: {}",
                            order.getOrderId().toString().substring(0, 8),
                            order.getUserName(),
                            String.format("%.2f", order.getFinalPrice()),
                            order.getOrderDate().toLocalDate(),
                            order.getStoreName(),
                            order.getPaymentMethod(),
                            order.getStatus());
                });

        // Address distribution summary
        logger.info("Address Distribution Summary:");
        users.stream()
                .filter(user -> !user.getAddressIds().isEmpty())
                .forEach(user -> {
                    List<Address> userAddresses = addressRepository.findByUsername(user.getUserName());
                    long defaultAddresses = userAddresses.stream().filter(Address::isDefault).count();
                    logger.info("- {}: {} addresses ({} default)",
                            user.getUserName(), userAddresses.size(), defaultAddresses);
                });
    }
}