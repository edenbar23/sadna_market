package com.sadna_market.market.UnitTests;

import com.sadna_market.market.ApplicationLayer.Requests.ProductRequest;
import com.sadna_market.market.ApplicationLayer.Requests.ProductSearchRequest;
import com.sadna_market.market.DomainLayer.IProductRepository;
//import com.sadna_market.market.ApplicationLayer.ProductRequest;
//import com.sadna_market.market.ApplicationLayer.ProductSearchRequest;
import com.sadna_market.market.DomainLayer.Product;
import com.sadna_market.market.InfrastructureLayer.RepositoryConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryProductRepositoryTest {

    private IProductRepository repository;
    private UUID storeId;
    private UUID productId;
    private String productName;
    private String category;
    private String description;
    private double price;
    private boolean isAvailable;
    private RepositoryConfiguration RC = new RepositoryConfiguration();

    @BeforeEach
    void setUp() {
       // repository = new InMemoryProductRepository();
       repository=RC.productRepository();
        // Set up test data
        storeId = UUID.randomUUID();
        productName = "Test Product";
        category = "Test Category";
        description = "Test Description";
        price = 99.99;
        isAvailable = true;
        
        // Add a product
        repository.addProduct(storeId, productName, category, description, price, isAvailable);
        
        // Get the productId by searching for the product
        List<Optional<Product>> products = repository.filterByName(productName);
        if (!products.isEmpty() && products.get(0).isPresent()) {
            productId = products.get(0).get().getProductId();
        }
    }

    @Test
    void testAddProduct() {
        // Verify the product was added in setUp()
        Optional<Product> product = repository.findById(productId);
        
        assertTrue(product.isPresent());
        assertEquals(productName, product.get().getName());
        assertEquals(category, product.get().getCategory());
        assertEquals(description, product.get().getDescription());
        assertEquals(price, product.get().getPrice());
        assertEquals(storeId, product.get().getStoreId());
        assertTrue(product.get().isAvailable());
    }

    @Test
    void testAddProductWithDuplicateName() {
        // Try to add a product with the same name
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.addProduct(storeId, productName, "Another Category", "Another Description", 59.99, true);
        });
        
        String expectedMessage = "Product with the same name already exists";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testFindById() {
        // Verify finding an existing product
        Optional<Product> product = repository.findById(productId);
        
        assertTrue(product.isPresent());
        assertEquals(productName, product.get().getName());
        
        // Verify finding a non-existent product
        UUID nonExistentId = UUID.randomUUID();
        Optional<Product> nonExistentProduct = repository.findById(nonExistentId);
        
        assertFalse(nonExistentProduct.isPresent());
    }

    @Test
    void testFilterByName() {
        // Filter by existing name
        List<Optional<Product>> products = repository.filterByName(productName);
        
        assertEquals(1, products.size());
        assertTrue(products.get(0).isPresent());
        assertEquals(productId, products.get(0).get().getProductId());
        
        // Filter by non-existent name
        products = repository.filterByName("Non-existent Product");
        
        assertTrue(products.isEmpty());
    }

    @Test
    void testFilterByCategory() {
        // Filter by existing category
        List<Optional<Product>> products = repository.filterByCategory(category);
        
        assertEquals(1, products.size());
        assertTrue(products.get(0).isPresent());
        assertEquals(productId, products.get(0).get().getProductId());
        
        // Filter by non-existent category
        products = repository.filterByCategory("Non-existent Category");
        
        assertTrue(products.isEmpty());
    }

    @Test
    void testFilterByPriceRange() {
        // Filter by price range that includes the product
        List<Optional<Product>> products = repository.filterByPriceRange(50.0, 150.0);
        
        assertEquals(1, products.size());
        assertTrue(products.get(0).isPresent());
        assertEquals(productId, products.get(0).get().getProductId());
        
        // Filter by price range that doesn't include the product
        products = repository.filterByPriceRange(10.0, 50.0);
        
        assertTrue(products.isEmpty());
    }

    @Test
    void testFilterByRate() {
        // Initially, the product has no ratings
        List<Optional<Product>> products = repository.filterByRate(0.0, 5.0);
        
        assertEquals(1, products.size());
        
        // Add a rating
        UUID userId = UUID.randomUUID();
        repository.handleUserRate(userId, productId, 4);
        
        // Filter by rate range that includes the product
        products = repository.filterByRate(3.0, 5.0);
        
        assertEquals(1, products.size());
        
        // Filter by rate range that doesn't include the product
        products = repository.filterByRate(4.5, 5.0);
        
        assertTrue(products.isEmpty());
    }

    @Test
    void testUpdateProduct() {
        // Create a ProductRequest for update
        String newName = "Updated Product";
        String newCategory = "Updated Category";
        String newDescription = "Updated Description";
        double newPrice = 149.99;
        
        ProductRequest updateRequest = new ProductRequest(
            productId, newName, newCategory, newDescription, newPrice
        );
        
        // Update the product
        repository.updateProduct(updateRequest);
        
        // Verify the update
        Optional<Product> updatedProduct = repository.findById(productId);
        
        assertTrue(updatedProduct.isPresent());
        assertEquals(newName, updatedProduct.get().getName());
        assertEquals(newCategory, updatedProduct.get().getCategory());
        assertEquals(newDescription, updatedProduct.get().getDescription());
        assertEquals(newPrice, updatedProduct.get().getPrice());
    }

    @Test
    void testUpdateProductWithNullId() {
        // Create a ProductRequest with null ID
        ProductRequest updateRequest = new ProductRequest(
          null,  "Updated Product", "Updated Category", "Updated Description", 149.99
        );
        
        // Try to update - should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.updateProduct(updateRequest);
        });
        
        String expectedMessage = "Product ID should not be null for existing products";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testUpdateNonExistentProduct() {
        // Create a ProductRequest with non-existent ID
        UUID nonExistentId = UUID.randomUUID();
        ProductRequest updateRequest = new ProductRequest(
            nonExistentId, "Updated Product", "Updated Category", "Updated Description", 149.99
        );
        
        // Try to update - should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.updateProduct(updateRequest);
        });
        
        String expectedMessage = "Product not found";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testDeleteProduct() {
        // Create a ProductRequest for delete
        ProductRequest deleteRequest = new ProductRequest(
            productId, productName, category, description, price
        );
        
        // Delete the product
        repository.deleteProduct(deleteRequest);
        
        // Verify the product is deleted
        Optional<Product> deletedProduct = repository.findById(productId);
        
        assertFalse(deletedProduct.isPresent());
    }

    @Test
    void testDeleteProductWithNullId() {
        // Create a ProductRequest with null ID
        ProductRequest deleteRequest = new ProductRequest(
           null, productName, category, description, price
        );
        
        // Try to delete - should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.deleteProduct(deleteRequest);
        });
        
        String expectedMessage = "Product ID should not be null for existing products";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testDeleteNonExistentProduct() {
        // Create a ProductRequest with non-existent ID
        UUID nonExistentId = UUID.randomUUID();
        ProductRequest deleteRequest = new ProductRequest(
            nonExistentId, "Non-existent Product", "Category", "Description", 99.99
        );
        
        // Try to delete - should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            repository.deleteProduct(deleteRequest);
        });
        
        String expectedMessage = "Product not found";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testGetProductsByIds() {
        // Add another product
        String anotherName = "Another Product";
        repository.addProduct(storeId, anotherName, category, description, price, isAvailable);
        
        // Get the ID of the new product
        List<Optional<Product>> products = repository.filterByName(anotherName);
        UUID anotherId = products.get(0).get().getProductId();
        
        // Create a set of IDs
        Set<UUID> ids = new HashSet<>();
        ids.add(productId);
        ids.add(anotherId);
        
        // Get products by IDs
        List<Optional<Product>> retrievedProducts = repository.getProductsByIds(ids);
        
        // Verify
        assertEquals(2, retrievedProducts.size());
        assertTrue(retrievedProducts.stream().allMatch(Optional::isPresent));
        assertTrue(retrievedProducts.stream()
            .map(p -> p.get().getProductId())
            .anyMatch(id -> id.equals(productId)));
        assertTrue(retrievedProducts.stream()
            .map(p -> p.get().getProductId())
            .anyMatch(id -> id.equals(anotherId)));
    }

    @Test
    void testGetProductsByIdsWithNonExistentIds() {
        // Create a set with one existing ID and one non-existent ID
        Set<UUID> ids = new HashSet<>();
        ids.add(productId);
        ids.add(UUID.randomUUID());
        
        // Get products by IDs
        List<Optional<Product>> retrievedProducts = repository.getProductsByIds(ids);
        
        // Verify
        assertEquals(2, retrievedProducts.size());
        // One present, one empty
        long presentCount = retrievedProducts.stream().filter(Optional::isPresent).count();
        assertEquals(1, presentCount);
    }

    @Test
    void testHandleUserRate() {
        // Create a user ID
        UUID userId = UUID.randomUUID();
        
        // Add a rating
        Optional<UserRate> userRateOpt = repository.handleUserRate(userId, productId, 4);
        
        // Verify
        assertTrue(userRateOpt.isPresent());
        UserRate userRate = userRateOpt.get();
        assertEquals(userId, userRate.getUserId());
        assertEquals(productId, userRate.getProductId());
        assertEquals(4, userRate.getRatingValue());
        
        // Check that the product's rating was updated
        Optional<Product> updatedProduct = repository.findById(productId);
        assertTrue(updatedProduct.isPresent());
        assertEquals(4.0, updatedProduct.get().getRate());
        assertEquals(1, updatedProduct.get().getNumOfRanks());
    }

    @Test
    void testHandleUserRateUpdate() {
        // Create a user ID
        UUID userId = UUID.randomUUID();
        
        // Add a rating
        repository.handleUserRate(userId, productId, 4);
        
        // Update the rating
        Optional<UserRate> updatedRateOpt = repository.handleUserRate(userId, productId, 5);
        
        // Verify
        assertTrue(updatedRateOpt.isPresent());
        UserRate updatedRate = updatedRateOpt.get();
        assertEquals(5, updatedRate.getRatingValue());
        
        // Check that the product's rating was updated
        Optional<Product> updatedProduct = repository.findById(productId);
        assertTrue(updatedProduct.isPresent());
        assertEquals(5.0, updatedProduct.get().getRate());
        assertEquals(1, updatedProduct.get().getNumOfRanks()); // Still just one rating
    }

    @Test
    void testHandleUserRateNonExistentProduct() {
        // Create a user ID
        UUID userId = UUID.randomUUID();
        
        // Try to rate a non-existent product
        UUID nonExistentId = UUID.randomUUID();
        Optional<UserRate> userRateOpt = repository.handleUserRate(userId, nonExistentId, 4);
        
        // Verify
        assertFalse(userRateOpt.isPresent());
    }

    @Test
    void testHandleUserReview() {
        // Create a user ID
        UUID userId = UUID.randomUUID();
        
        // Add a review
        String reviewText = "This is a test review";
        repository.handleUserReview(userId, productId, reviewText);
        
        // We don't have a direct way to verify the review was added since there's no getter,
        // but we can at least verify no exception is thrown
    }

    @Test
    void testHandleUserReviewNonExistentProduct() {
        // Create a user ID
        UUID userId = UUID.randomUUID();
        
        // Try to review a non-existent product
        UUID nonExistentId = UUID.randomUUID();
        String reviewText = "This is a test review";
        
        // Should not throw an exception, but log an error instead
        repository.handleUserReview(userId, nonExistentId, reviewText);
    }

    @Test
    void testSearchProduct() {
        // Create a search request matching our product
        ProductSearchRequest request = new ProductSearchRequest();
        request.setName(productName);
        request.setCategory(category);
        request.setMinPrice(50.0);
        request.setMaxPrice(150.0);
        request.setMinRank(0.0);
        request.setMaxRank(5.0);
        
        // Search for products
        List<Optional<Product>> results = repository.searchProduct(request);
        
        // Verify
        assertEquals(1, results.size());
        assertTrue(results.get(0).isPresent());
        assertEquals(productId, results.get(0).get().getProductId());
    }

    @Test
    void testSearchProductNoResults() {
        // Create a search request that won't match any product
        ProductSearchRequest request = new ProductSearchRequest();
        request.setName("Non-existent");
        
        // Search for products
        List<Optional<Product>> results = repository.searchProduct(request);
        
        // Verify
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindByStoreId() {
        // Find products by our store ID
        List<Optional<Product>> products = repository.findByStoreId(storeId);
        
        // Verify
        assertEquals(1, products.size());
        assertTrue(products.get(0).isPresent());
        assertEquals(productId, products.get(0).get().getProductId());
        
        // Find products by a non-existent store ID
        UUID nonExistentStoreId = UUID.randomUUID();
        products = repository.findByStoreId(nonExistentStoreId);
        
        // Verify
        assertTrue(products.isEmpty());
    }

    @Test
    void testFilterByStoreWithRequest() {
        // Create a search request matching our product
        ProductSearchRequest request = new ProductSearchRequest();
        request.setName(productName);
        
        // Search for products in our store
        List<Optional<Product>> results = repository.filterByStoreWithRequest(storeId, request);
        
        // Verify
        assertEquals(1, results.size());
        assertTrue(results.get(0).isPresent());
        assertEquals(productId, results.get(0).get().getProductId());
        
        // Search for products in a non-existent store
        UUID nonExistentStoreId = UUID.randomUUID();
        results = repository.filterByStoreWithRequest(nonExistentStoreId, request);
        
        // Verify
        assertTrue(results.isEmpty());
    }
}
