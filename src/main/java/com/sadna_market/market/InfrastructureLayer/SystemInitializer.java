//package com.sadna_market.market.InfrastructureLayer;
//
//import com.sadna_market.market.DomainLayer.*;
//import com.sadna_market.market.DomainLayer.DomainServices.InventoryManagementService;
//import com.sadna_market.market.DomainLayer.DomainServices.RatingService;
//import com.sadna_market.market.DomainLayer.DomainServices.StoreManagementService;
//import com.sadna_market.market.DomainLayer.DomainServices.UserAccessService;
//import com.sadna_market.market.InfrastructureLayer.Authentication.IAuthRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//
///**
// * This class initializes the system with sample data for testing and demonstration purposes.
// * It is run at application startup when the "dev" profile is active.
// */
//@Component
//@Profile("dev")
//public class SystemInitializer implements CommandLineRunner {
//    private static final Logger logger = LoggerFactory.getLogger(SystemInitializer.class);
//
//    private final IUserRepository userRepository;
//    private final IStoreRepository storeRepository;
//    private final IProductRepository productRepository;
//    private final IOrderRepository orderRepository;
//    private final IMessageRepository messageRepository;
//    private final IReportRepository reportRepository;
//    private final IRatingRepository ratingRepository;
//    private final IAuthRepository authRepository;
//    private final UserAccessService userAccessService;
//    private final StoreManagementService storeManagementService;
//    private final InventoryManagementService inventoryManagementService;
//    private final RatingService ratingService;
//
//    @Autowired
//    public SystemInitializer(
//            IUserRepository userRepository,
//            IStoreRepository storeRepository,
//            IProductRepository productRepository,
//            IOrderRepository orderRepository,
//            IMessageRepository messageRepository,
//            IReportRepository reportRepository,
//            IRatingRepository ratingRepository,
//            IAuthRepository authRepository,
//            UserAccessService userAccessService,
//            StoreManagementService storeManagementService,
//            InventoryManagementService inventoryManagementService,
//            RatingService ratingService) {
//        this.userRepository = userRepository;
//        this.storeRepository = storeRepository;
//        this.productRepository = productRepository;
//        this.orderRepository = orderRepository;
//        this.messageRepository = messageRepository;
//        this.reportRepository = reportRepository;
//        this.ratingRepository = ratingRepository;
//        this.authRepository = authRepository;
//        this.userAccessService = userAccessService;
//        this.storeManagementService = storeManagementService;
//        this.inventoryManagementService = inventoryManagementService;
//        this.ratingService = ratingService;
//    }
//
//    @Override
//    public void run(String... args) {
//        logger.info("Initializing system with sample data...");
//
//        try {
//            // Clear existing data
//            clearAllData();
//
//            // Initialize users
//            initializeUsers();
//
//            // Initialize stores
//            Map<String, UUID> storeIds = initializeStores();
//
//            // Initialize products
//            initializeProducts(storeIds);
//
//            // Initialize store personnel (owners and managers)
//            initializeStorePersonnel(storeIds);
//
//            // Initialize ratings and reviews
//            initializeRatingsAndReviews(storeIds);
//
//            // Initialize messages
//            initializeMessages(storeIds);
//
//            logger.info("System successfully initialized with sample data");
//        } catch (Exception e) {
//            logger.error("Error initializing system with sample data: {}", e.getMessage(), e);
//        }
//    }
//
//    private void clearAllData() {
//        logger.info("Clearing existing data...");
//        userRepository.clear();
//        storeRepository.clear();
//        productRepository.clear();
//        orderRepository.clear();
//        messageRepository.clear();
//        reportRepository.clear();
//        ratingRepository.clear();
//        authRepository.clear();
//    }
//
//    private void initializeUsers() {
//        logger.info("Initializing users with Pokémon names...");
//
//        // Create admin user
//        User admin = new User("admin", "Admin123!", "admin@market.com", "System", "Admin");
//        userRepository.save(admin);
//        authRepository.addUser("admin", "Admin123!");
//
//        // Create regular users with Pokémon names
//        String[][] userData = {
//                // username, password, email, firstName, lastName
//                {"pikachu", "Password1!", "pikachu@example.com", "Pika", "Chu"},
//                {"charizard", "Password1!", "charizard@example.com", "Chari", "Zard"},
//                {"bulbasaur", "Password1!", "bulbasaur@example.com", "Bulba", "Saur"},
//                {"squirtle", "Password1!", "squirtle@example.com", "Squir", "Tle"},
//                {"mewtwo", "Password1!", "mewtwo@example.com", "Mew", "Two"}
//        };
//
//        for (String[] user : userData) {
//            userAccessService.registerUser(user[0], user[1], user[2], user[3], user[4]);
//            authRepository.addUser(user[0], user[1]);
//        }
//    }
//
//    private Map<String, UUID> initializeStores() {
//        logger.info("Initializing stores...");
//        Map<String, UUID> storeIds = new HashMap<>();
//
//        // Create stores with different owners (Pokémon trainers)
//        // Store 1: Electronics Store (Owner: pikachu)
//        Store electronicsStore = storeManagementService.createStore(
//                "pikachu",
//                "Electric Type Gadgets",
//                "Shocking deals on all electronics",
//                "123 Thunderbolt Ave, Viridian City",
//                "contact@electrictype.com",
//                "555-PIKA-CHU"
//        );
//        storeIds.put("electronics", electronicsStore.getStoreId());
//
//        // Store 2: Clothing Store (Owner: charizard)
//        Store clothingStore = storeManagementService.createStore(
//                "charizard",
//                "Fire Fashion",
//                "Hot styles that will burn up the competition",
//                "456 Flame St, Cinnabar Island",
//                "info@firefashion.com",
//                "555-CHAR-IZAR"
//        );
//        storeIds.put("clothing", clothingStore.getStoreId());
//
//        // Store 3: Grocery Store (Owner: bulbasaur)
//        Store groceryStore = storeManagementService.createStore(
//                "bulbasaur",
//                "Grass Grocers",
//                "Fresh organic produce grown with solar energy",
//                "789 Vine Whip Road, Celadon City",
//                "orders@grassgrocers.com",
//                "555-BULBA-SAUR"
//        );
//        storeIds.put("grocery", groceryStore.getStoreId());
//
//        return storeIds;
//    }
//
//    private void initializeProducts(Map<String, UUID> storeIds) {
//        logger.info("Initializing products...");
//
//        // Electronics store products
//        UUID electronicsStoreId = storeIds.get("electronics");
//        Object[][] electronicsProducts = {
//                // name, category, description, price, quantity
//                {"Thunder Phone", "Phones", "Charges incredibly fast with electric-type technology", 899.99, 10},
//                {"Volt Laptop", "Computers", "Powered by lightning for maximum performance", 1299.99, 5},
//                {"Static Headphones", "Audio", "Noise-cancelling with electric field technology", 199.99, 15},
//                {"Spark TV 55\"", "TVs", "4K display with lightning-fast refresh rate", 699.99, 8},
//                {"Jolteon Gaming Console", "Gaming", "Electrifying gaming experience", 499.99, 7}
//        };
//
//        for (Object[] product : electronicsProducts) {
//            inventoryManagementService.addProductToStore(
//                    "pikachu",
//                    electronicsStoreId,
//                    (String) product[0],
//                    (String) product[1],
//                    (String) product[2],
//                    (double) product[3],
//                    (int) product[4]
//            );
//        }
//
//        // Clothing store products
//        UUID clothingStoreId = storeIds.get("clothing");
//        Object[][] clothingProducts = {
//                // name, category, description, price, quantity
//                {"Blaze T-Shirt", "Men", "Fire-resistant cotton blend t-shirt", 24.99, 20},
//                {"Inferno Dress", "Women", "Hot summer dress that's always in season", 49.99, 15},
//                {"Ember Shorts", "Children", "Keeps your little ones warm in any weather", 19.99, 25},
//                {"Flame Jeans", "Men", "Stylish jeans with flame patterns", 59.99, 18},
//                {"Heat Wave Hat", "Accessories", "Keeps you cool while looking hot", 29.99, 12}
//        };
//
//        for (Object[] product : clothingProducts) {
//            inventoryManagementService.addProductToStore(
//                    "charizard",
//                    clothingStoreId,
//                    (String) product[0],
//                    (String) product[1],
//                    (String) product[2],
//                    (double) product[3],
//                    (int) product[4]
//            );
//        }
//
//        // Grocery store products
//        UUID groceryStoreId = storeIds.get("grocery");
//        Object[][] groceryProducts = {
//                // name, category, description, price, quantity
//                {"Pecha Berries", "Fruit", "Naturally sweet and cures poisoning", 3.99, 30},
//                {"Solar Bread", "Bakery", "Baked using only sunlight energy", 4.99, 25},
//                {"Leaf Eggs", "Dairy", "Laid by the finest Exeggcute", 5.99, 40},
//                {"Grass-Fed Tauros Beef", "Meat", "Ethically raised Tauros meat", 8.99, 20},
//                {"Oddish Spinach", "Vegetables", "Fresh picked from our garden", 3.49, 35}
//        };
//
//        for (Object[] product : groceryProducts) {
//            inventoryManagementService.addProductToStore(
//                    "bulbasaur",
//                    groceryStoreId,
//                    (String) product[0],
//                    (String) product[1],
//                    (String) product[2],
//                    (double) product[3],
//                    (int) product[4]
//            );
//        }
//    }
//
//    private void initializeStorePersonnel(Map<String, UUID> storeIds) {
//        logger.info("Initializing store personnel...");
//
//        // Add squirtle as a manager in the electronics store
//        UUID electronicsStoreId = storeIds.get("electronics");
//        Set<Permission> electronicsManagerPermissions = new HashSet<>(Arrays.asList(
//                Permission.MANAGE_INVENTORY,
//                Permission.ADD_PRODUCT,
//                Permission.REMOVE_PRODUCT,
//                Permission.UPDATE_PRODUCT,
//                Permission.RESPOND_TO_USER_INQUIRIES
//        ));
//        storeManagementService.appointStoreManager("pikachu", electronicsStoreId, "squirtle", electronicsManagerPermissions);
//
//        // Add mewtwo as another owner in the clothing store
//        UUID clothingStoreId = storeIds.get("clothing");
//        storeManagementService.appointStoreOwner("charizard", clothingStoreId, "mewtwo");
//
//        // Add squirtle as a manager in the grocery store too
//        UUID groceryStoreId = storeIds.get("grocery");
//        Set<Permission> groceryManagerPermissions = new HashSet<>(Arrays.asList(
//                Permission.MANAGE_INVENTORY,
//                Permission.RESPOND_TO_USER_INQUIRIES,
//                Permission.VIEW_STORE_PURCHASE_HISTORY
//        ));
//        storeManagementService.appointStoreManager("bulbasaur", groceryStoreId, "squirtle", groceryManagerPermissions);
//    }
//
//    private void initializeRatingsAndReviews(Map<String, UUID> storeIds) {
//        logger.info("Initializing ratings and reviews...");
//
//        // Get product IDs from the repositories
//        List<Optional<Product>> electronicsProducts = productRepository.findByStoreId(storeIds.get("electronics"));
//        List<Optional<Product>> clothingProducts = productRepository.findByStoreId(storeIds.get("clothing"));
//        List<Optional<Product>> groceryProducts = productRepository.findByStoreId(storeIds.get("grocery"));
//
//        // Add some ratings and reviews to electronics products
//        if (!electronicsProducts.isEmpty() && electronicsProducts.get(0).isPresent()) {
//            Product smartphone = electronicsProducts.get(0).get();
//            // Simulate that bulbasaur has purchased this product
//            Order order = new Order(
//                    smartphone.getStoreId(),
//                    "bulbasaur",
//                    new HashMap<UUID, Integer>() {{ put(smartphone.getProductId(), 1); }},
//                    smartphone.getPrice(),
//                    smartphone.getPrice(),
//                    java.time.LocalDateTime.now(),
//                    OrderStatus.PAID,
//                    UUID.randomUUID()
//            );
//            orderRepository.save(order);
//
//            // bulbasaur rates and reviews the Thunder Phone
//            ratingService.rateProduct("bulbasaur", smartphone.getProductId(), 5);
//            ratingService.reviewProduct("bulbasaur", smartphone.getProductId(), smartphone.getStoreId(),
//                    "Super effective! This phone charges even faster than my Solar Beam attack!");
//        }
//
//        if (electronicsProducts.size() > 1 && electronicsProducts.get(1).isPresent()) {
//            Product laptop = electronicsProducts.get(1).get();
//            // Simulate that charizard has purchased this product
//            Order order = new Order(
//                    laptop.getStoreId(),
//                    "charizard",
//                    new HashMap<UUID, Integer>() {{ put(laptop.getProductId(), 1); }},
//                    laptop.getPrice(),
//                    laptop.getPrice(),
//                    java.time.LocalDateTime.now(),
//                    OrderStatus.PAID,
//                    UUID.randomUUID()
//            );
//            orderRepository.save(order);
//
//            // charizard rates and reviews the Volt Laptop
//            ratingService.rateProduct("charizard", laptop.getProductId(), 4);
//            ratingService.reviewProduct("charizard", laptop.getProductId(), laptop.getStoreId(),
//                    "Almost as powerful as my Flamethrower! But it does get a bit hot when processing heavy tasks.");
//        }
//
//        // Add some ratings to clothing products
//        if (!clothingProducts.isEmpty() && clothingProducts.get(0).isPresent()) {
//            Product tshirt = clothingProducts.get(0).get();
//            // Simulate that bulbasaur has purchased this product
//            Order order = new Order(
//                    tshirt.getStoreId(),
//                    "bulbasaur",
//                    new HashMap<UUID, Integer>() {{ put(tshirt.getProductId(), 2); }},
//                    tshirt.getPrice() * 2,
//                    tshirt.getPrice() * 2,
//                    java.time.LocalDateTime.now(),
//                    OrderStatus.PAID,
//                    UUID.randomUUID()
//            );
//            orderRepository.save(order);
//
//            // bulbasaur rates the Blaze T-Shirt
//            ratingService.rateProduct("bulbasaur", tshirt.getProductId(), 5);
//        }
//
//        // Add store ratings
//        ratingService.rateStore("charizard", storeIds.get("electronics"), 5,
//                "Pikachu's store has shocking deals! Everything is electrifying!");
//        ratingService.rateStore("bulbasaur", storeIds.get("clothing"), 4,
//                "Charizard's fashion is hot, but sometimes too hot for my grass type!");
//        ratingService.rateStore("pikachu", storeIds.get("grocery"), 5,
//                "Bulbasaur's organic produce is simply the best! Pika Pika approved!");
//    }
//
//    private void initializeMessages(Map<String, UUID> storeIds) {
//        logger.info("Initializing messages...");
//
//        // Add messages to the electronics store
//        Message electronicsMessage1 = new Message(
//                "charizard",
//                storeIds.get("electronics"),
//                "Do you have the Thunder Phone in red color to match my fire type?"
//        );
//        messageRepository.save(electronicsMessage1);
//
//        // Add a message with a reply
//        Message electronicsMessage2 = new Message(
//                "bulbasaur",
//                storeIds.get("electronics"),
//                "Is the Jolteon Gaming Console compatible with grass-type games?"
//        );
//        messageRepository.save(electronicsMessage2);
//        messageRepository.addReply(
//                electronicsMessage2.getMessageId(),
//                "squirtle", // The manager replies
//                "Yes, it's compatible with all Pokémon types! We even have a special Venusaur edition coming soon. Let me know if you have any other questions!"
//        );
//
//        // Add messages to the clothing store
//        Message clothingMessage = new Message(
//                "pikachu",
//                storeIds.get("clothing"),
//                "Do the Blaze T-Shirts come in yellow with lightning bolts?"
//        );
//        messageRepository.save(clothingMessage);
//
//        // Add messages to the grocery store
//        Message groceryMessage = new Message(
//                "mewtwo",
//                storeIds.get("grocery"),
//                "Do you have any Rare Candies in stock? I need to level up."
//        );
//        messageRepository.save(groceryMessage);
//    }
//}