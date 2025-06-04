package com.sadna_market.market.DomainLayer;

import lombok.Getter;
import lombok.Setter;
import java.util.*;
import jakarta.persistence.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Entity
@Table(name = "stores")
@Getter
public class Store {
    // Basic properties
    @Id
    @Column(name = "store_id", updatable = false, nullable = false)
    private UUID storeId;

    @Setter
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Setter
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "creation_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    // Rating properties
    @Column(name = "rating", nullable = false)
    private double rating;

    @Column(name = "rating_count", nullable = false)
    private int ratingCount;

    // StoreFounder handling - save only username in DB but keep founder field as StoreFounder
    @Transient // This field won't be persisted directly
    private StoreFounder founder;

    @Column(name = "founder_username", length = 50)
    private String founderUsername; // This will be persisted

    // Collections using @ElementCollection with EAGER fetch (no lazy loading)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "store_product_quantities",
            joinColumns = @JoinColumn(name = "store_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> productQuantities = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "store_owners",
            joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "username", length = 50)
    private Set<String> ownerUsernames = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "store_managers",
            joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "username", length = 50)
    private Set<String> managerUsernames = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "store_orders",
            joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "order_id")
    private Set<UUID> orderIds = new HashSet<>();

    // Use a single ReentrantReadWriteLock for better deadlock prevention
    @Transient // Don't persist the lock
    private final ReentrantReadWriteLock storeLock = new ReentrantReadWriteLock();


    // JPA lifecycle methods to handle StoreFounder conversion
    @PostLoad
    private void initializeFounderFromUsername() {
        if (founderUsername != null) {
            this.founder = new StoreFounder(founderUsername, this.storeId, null);
        }
    }

    @PrePersist
    @PreUpdate
    private void saveFounderUsername() {
        if (founder != null) {
            this.founderUsername = founder.getUsername();
        }
    }

    // Constructors
    public Store() {
        this.storeId = UUID.randomUUID();
        this.creationDate = new Date();
        this.active = true;
        this.founder = null;
    }

    public Store(String name, String description) {
        validateConstructorParameters(name);

        this.storeId = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.active = true;
        this.creationDate = new Date();
        this.founder = null; // Will be set later
        this.rating = 0.0;
        this.ratingCount = 0;
    }

    public Store(String name, String description, StoreFounder founder) {
        validateConstructorParameters(name, founder);

        this.storeId = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.active = true;
        this.creationDate = new Date();
        this.founder = null;
        this.rating = 0.0;
        this.ratingCount = 0;

        // If founder is provided, set it properly
        if (founder != null) {
            setFounder(founder);
        }
        this.founderUsername = founder != null ? founder.getUsername() : null;
    }

    // Full constructor for repository reconstruction
    public Store(UUID storeId, String name, String description, boolean active,
                 Date creationDate, StoreFounder founder) {
        validateConstructorParameters(name, founder);

        this.storeId = storeId;
        this.name = name;
        this.description = description;
        this.active = active;
        this.creationDate = creationDate;
        this.founder = founder;

        this.rating = 0.0;
        this.ratingCount = 0;

        // Add the founder as the first owner
        if (founder != null) {
            this.ownerUsernames.add(founder.getUsername());
        }
        this.founderUsername = founder != null ? founder.getUsername() : null;

    }

    public void setFounder(StoreFounder founder) {
        if (this.founder != null) {
            throw new IllegalStateException("Store already has a founder");
        }
        if (founder == null) {
            throw new IllegalArgumentException("Founder cannot be null");
        }

        if (!founder.getStoreId().equals(this.storeId)) {
            throw new IllegalArgumentException("Founder's store ID does not match this store's ID");
        }


        this.founder = founder;
        this.founderUsername = founder.getUsername(); // Update the persisted field
        this.ownerUsernames.add(founder.getUsername());
    }

    // Validation methods
    private void validateConstructorParameters(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Store name cannot be null or empty");
        }
    }

    private void validateConstructorParameters(String name, StoreFounder founder) {
        validateConstructorParameters(name);
        if (founder == null) {
            throw new IllegalArgumentException("Store founder cannot be null");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Product quantity cannot be negative");
        }
    }

    private void validateProduct(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (!productQuantities.containsKey(productId)) {
            throw new IllegalArgumentException("Product does not exist in store");
        }
    }

    private void validateStoreActive() {
        if (!isActive()) {
            throw new IllegalStateException("Store is not active");
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
    }

    private void validateOrder(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
    }

    private void validateRating(int ratingValue) {
        if (ratingValue < 1 || ratingValue > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }


    // Rating methods
    public void addRating(int ratingValue) {
        validateRating(ratingValue);

        storeLock.writeLock().lock();
        try {
            // Calculate new average without storing the sum
            rating = (rating * ratingCount + ratingValue) / (ratingCount + 1);
            ratingCount++;
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    public void updateRating(int oldRate, int newRate) {
        validateRating(newRate);

        storeLock.writeLock().lock();
        try {
            if (ratingCount == 0) {
                throw new IllegalStateException("No ratings to update");
            }
            double total = rating * ratingCount;
            total = total - oldRate + newRate;
            rating = total / ratingCount;
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    public double getStoreRating() {
        storeLock.readLock().lock();
        try {
            return rating;
        } finally {
            storeLock.readLock().unlock();
        }
    }

    public int getNumOfRatings() {
        storeLock.readLock().lock();
        try {
            return ratingCount;
        } finally {
            storeLock.readLock().unlock();
        }
    }

    // Store status methods
    public boolean isActive() {
        storeLock.readLock().lock();
        try {
            return active;
        } finally {
            storeLock.readLock().unlock();
        }
    }

    public void closeStore() {
        storeLock.writeLock().lock();
        try {
            if (!active) {
                throw new IllegalStateException("Store is already closed");
            }
            this.active = false;
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    public void reopenStore() {
        storeLock.writeLock().lock();
        try {
            if (active) {
                throw new IllegalStateException("Store is already open");
            }
            this.active = true;
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    // Product methods
    public void addProduct(UUID productId, int quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        validateQuantity(quantity);

        storeLock.writeLock().lock();
        try {
            validateStoreActive();

            if (productQuantities.containsKey(productId)) {
                throw new IllegalArgumentException("Product already exists in store");
            }

            productQuantities.put(productId, quantity);
            System.out.println("Product added: " + productId + ", Quantity: " + quantity);
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    public void updateProductQuantity(UUID productId, int newQuantity) {
        validateQuantity(newQuantity);

        storeLock.writeLock().lock();
        try {
            validateStoreActive();
            validateProduct(productId);

            productQuantities.put(productId, newQuantity);
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    public void removeProduct(UUID productId) {
        storeLock.writeLock().lock();
        try {
            validateStoreActive();
            validateProduct(productId);

            productQuantities.remove(productId);
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    public boolean hasProduct(UUID productId) {
        if (productId == null) {
            return false;
        }

        storeLock.readLock().lock();
        try {
            return productQuantities.containsKey(productId);
        } finally {
            storeLock.readLock().unlock();
        }
    }

    public int getProductQuantity(UUID productId) {
        storeLock.readLock().lock();
        try {
            Integer quantity = productQuantities.get(productId);
            if (quantity == null) {
                throw new IllegalArgumentException("Product does not exist in store");
            }
            return quantity;
        } finally {
            storeLock.readLock().unlock();
        }
    }

    // Store personnel methods
    public void addStoreOwner(String newOwnerUsername) {
        validateUsername(newOwnerUsername);

        storeLock.writeLock().lock();
        try {
            validateStoreActive();

            if (isStoreOwner(newOwnerUsername)) {
                throw new IllegalArgumentException("User is already an owner of this store");
            }

            ownerUsernames.add(newOwnerUsername);
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    public void addStoreManager(String newManagerUsername) {
        validateUsername(newManagerUsername);

        storeLock.writeLock().lock();
        try {
            validateStoreActive();

            if (isStoreManager(newManagerUsername)) {
                throw new IllegalArgumentException("User is already a manager of this store");
            }

            managerUsernames.add(newManagerUsername);
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    public void removeStoreOwner(String ownerUsername) {
        validateUsername(ownerUsername);

        storeLock.writeLock().lock();
        try {
            validateStoreActive();

            if (isFounder(ownerUsername)) {
                throw new IllegalArgumentException("Cannot remove the founder of the store");
            }

            if (!isStoreOwner(ownerUsername)) {
                throw new IllegalArgumentException("User is not an owner of this store");
            }

            ownerUsernames.remove(ownerUsername);
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    public void removeStoreManager(String managerUsername) {
        validateUsername(managerUsername);

        storeLock.writeLock().lock();
        try {
            validateStoreActive();

            if (!isStoreManager(managerUsername)) {
                throw new IllegalArgumentException("User is not a manager of this store");
            }

            managerUsernames.remove(managerUsername);
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    public boolean isFounder(String username) {
        if (username == null) {
            return false;
        }
        return founder != null && founder.getUsername().equals(username);
    }

    public boolean isStoreOwner(String username) {
        if (username == null) {
            return false;
        }

        storeLock.readLock().lock();
        try {
            return ownerUsernames.contains(username);
        } finally {
            storeLock.readLock().unlock();
        }
    }

    public boolean isStoreManager(String username) {
        if (username == null) {
            return false;
        }

        storeLock.readLock().lock();
        try {
            return managerUsernames.contains(username);
        } finally {
            storeLock.readLock().unlock();
        }
    }

    // Order methods
    public void addOrder(UUID orderId) {
        validateOrder(orderId);

        storeLock.writeLock().lock();
        try {
            validateStoreActive();

            if (orderIds.contains(orderId)) {
                throw new IllegalArgumentException("Order already exists in store");
            }

            orderIds.add(orderId);
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    // Shopping cart and purchase methods
    public Set<String> checkCart(Map<UUID, Integer> items) {
        if (items == null) {
            throw new IllegalArgumentException("Items cannot be null");
        }

        Set<String> errors = new HashSet<>();

        storeLock.readLock().lock();
        try {
            if (!isActive()) {
                errors.add("Store is not active");
                return errors;
            }

            for (Map.Entry<UUID, Integer> entry : items.entrySet()) {
                UUID productId = entry.getKey();
                Integer requestedQuantity = entry.getValue();

                if (!productQuantities.containsKey(productId)) {
                    errors.add("Product " + productId + " does not exist in store");
                } else if (requestedQuantity > productQuantities.get(productId)) {
                    errors.add("Not enough stock for product " + productId +
                            ". Available: " + productQuantities.get(productId) +
                            ", Requested: " + requestedQuantity);
                }
            }
        } finally {
            storeLock.readLock().unlock();
        }

        return errors;
    }

    public Set<String> updateStockAfterPurchase(Map<UUID, Integer> items) {
        if (items == null) {
            throw new IllegalArgumentException("Items cannot be null");
        }

        storeLock.writeLock().lock();
        try {
            Set<String> checkResult = checkCart(items);
            if (!checkResult.isEmpty()) {
                return checkResult;
            }

            // Update quantities
            for (Map.Entry<UUID, Integer> entry : items.entrySet()) {
                UUID productId = entry.getKey();
                Integer purchasedQuantity = entry.getValue();

                Integer currentQuantity = productQuantities.get(productId);
                productQuantities.put(productId, currentQuantity - purchasedQuantity);
            }

            return new HashSet<>();
        } finally {
            storeLock.writeLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return storeId.equals(store.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId);
    }

    @Override
    public String toString() {
        return "Store{" +
                "name='" + name + '\'' +
                ", storeId=" + storeId +
                ", active=" + active +
                ", numberOfProducts=" + productQuantities.size() +
                ", numberOfOwners=" + ownerUsernames.size() +
                ", numberOfManagers=" + managerUsernames.size() +
                '}';
    }
}