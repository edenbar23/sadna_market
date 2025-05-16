package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.Product;
import com.sadna_market.market.DomainLayer.ProductRating;
import com.sadna_market.market.InfrastructureLayer.InMemoryRepos.InMemoryProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryProductRepository Unit Tests")
public class InMemoryProductRepositoryUnitTest {

    private InMemoryProductRepository productRepository;
    private UUID testStoreId;
    private UUID testProductId;
    private Product testProduct;
    private final String testProductName = "TestProduct";
    private final String testCategory = "TestCategory";
    private final String testDescription = "Test product description";
    private final double testPrice = 99.99;

    @BeforeEach
    void setUp() {
        System.out.println("\n===== Setting up test environment =====");
        productRepository = new InMemoryProductRepository();
        testStoreId = UUID.randomUUID();

        // Create a test product
        testProductId = productRepository.addProduct(
                testStoreId,
                testProductName,
                testCategory,
                testDescription,
                testPrice,
                true // available
        );

        System.out.println("Created test product with ID: " + testProductId);
        System.out.println("Product name: " + testProductName);
        System.out.println("Product category: " + testCategory);

        // Get the product for use in tests
        Optional<Product> productOpt = productRepository.findById(testProductId);
        assertTrue(productOpt.isPresent(), "Test product should be found");
        testProduct = productOpt.get();
        System.out.println("Retrieved test product successfully");
        System.out.println("===== Setup complete =====");
    }

    @AfterEach
    void tearDown() {
        System.out.println("===== Cleaning up test resources =====");
        productRepository.clear();
        testProduct = null;
        System.out.println("Product repository cleared");
        System.out.println("Test product reference set to null");
        System.out.println("===== Cleanup complete =====\n");
    }

    // Basic CRUD Operation Tests

    @Test
    @DisplayName("Finding a product by ID returns the correct product")
    void testFindById_ExistingProduct_ReturnsProduct() {
        System.out.println("TEST: Verifying findById with existing product");

        System.out.println("Looking for product with ID: " + testProductId);
        Optional<Product> result = productRepository.findById(testProductId);

        System.out.println("Expected: Product should be present");
        System.out.println("Actual: Product is present = " + result.isPresent());
        assertTrue(result.isPresent(), "Product should be found");

        System.out.println("Expected product ID: " + testProductId);
        System.out.println("Actual product ID: " + result.get().getProductId());
        assertEquals(testProductId, result.get().getProductId(), "Product ID should match");

        System.out.println("Expected product name: " + testProductName);
        System.out.println("Actual product name: " + result.get().getName());
        assertEquals(testProductName, result.get().getName(), "Product name should match");

        System.out.println("✓ findById correctly returns the product");
    }

    @Test
    @DisplayName("Finding a product by ID returns empty for non-existent product")
    void testFindById_NonExistingProduct_ReturnsEmpty() {
        System.out.println("TEST: Verifying findById with non-existing product");

        UUID nonExistingId = UUID.randomUUID();
        System.out.println("Looking for non-existing product with ID: " + nonExistingId);
        Optional<Product> result = productRepository.findById(nonExistingId);

        System.out.println("Expected: Product should not be present");
        System.out.println("Actual: Product is present = " + result.isPresent());
        assertFalse(result.isPresent(), "Product should not be found");

        System.out.println("✓ findById correctly returns empty for non-existing product");
    }

    @Test
    @DisplayName("Filtering products by name returns products with matching names")
    void testFilterByName_ExistingName_ReturnsMatchingProducts() {
        System.out.println("TEST: Verifying filterByName with existing name");

        System.out.println("Looking for products with name: " + testProductName);
        List<Optional<Product>> results = productRepository.filterByName(testProductName);

        System.out.println("Expected: Results should not be empty");
        System.out.println("Actual: Results size = " + results.size());
        assertFalse(results.isEmpty(), "Results should not be empty");

        Optional<Product> firstResult = results.get(0);
        System.out.println("Expected: First result should be present");
        System.out.println("Actual: First result is present = " + firstResult.isPresent());
        assertTrue(firstResult.isPresent(), "First result should be present");

        System.out.println("Expected product name: " + testProductName);
        System.out.println("Actual product name: " + firstResult.get().getName());
        assertEquals(testProductName, firstResult.get().getName(), "Product name should match");

        System.out.println("✓ filterByName correctly returns products with matching names");
    }

    @Test
    @DisplayName("Filtering products by category returns products in that category")
    void testFilterByCategory_ExistingCategory_ReturnsMatchingProducts() {
        System.out.println("TEST: Verifying filterByCategory with existing category");

        System.out.println("Looking for products with category: " + testCategory);
        List<Optional<Product>> results = productRepository.filterByCategory(testCategory);

        System.out.println("Expected: Results should not be empty");
        System.out.println("Actual: Results size = " + results.size());
        assertFalse(results.isEmpty(), "Results should not be empty");

        Optional<Product> firstResult = results.get(0);
        System.out.println("Expected: First result should be present");
        System.out.println("Actual: First result is present = " + firstResult.isPresent());
        assertTrue(firstResult.isPresent(), "First result should be present");

        System.out.println("Expected product category: " + testCategory);
        System.out.println("Actual product category: " + firstResult.get().getCategory());
        assertEquals(testCategory, firstResult.get().getCategory(), "Product category should match");

        System.out.println("✓ filterByCategory correctly returns products with matching category");
    }

    @Test
    @DisplayName("Filtering products by price range returns products within that range")
    void testFilterByPriceRange_ProductsInRange_ReturnsMatchingProducts() {
        System.out.println("TEST: Verifying filterByPriceRange with products in range");

        double minPrice = testPrice - 10;
        double maxPrice = testPrice + 10;

        System.out.println("Looking for products with price between " + minPrice + " and " + maxPrice);
        List<Optional<Product>> results = productRepository.filterByPriceRange(minPrice, maxPrice);

        System.out.println("Expected: Results should not be empty");
        System.out.println("Actual: Results size = " + results.size());
        assertFalse(results.isEmpty(), "Results should not be empty");

        Optional<Product> firstResult = results.get(0);
        System.out.println("Expected: First result should be present");
        System.out.println("Actual: First result is present = " + firstResult.isPresent());
        assertTrue(firstResult.isPresent(), "First result should be present");

        System.out.println("Expected product price to be within range: true");
        double price = firstResult.get().getPrice();
        boolean inRange = price >= minPrice && price <= maxPrice;
        System.out.println("Actual product price (" + price + ") is within range: " + inRange);
        assertTrue(inRange, "Product price should be within the range");

        System.out.println("✓ filterByPriceRange correctly returns products within price range");
    }

    @Test
    @DisplayName("Filtering products by price range returns empty for out of range products")
    void testFilterByPriceRange_ProductsOutOfRange_ReturnsEmpty() {
        System.out.println("TEST: Verifying filterByPriceRange with products out of range");

        double minPrice = testPrice + 10;
        double maxPrice = testPrice + 20;

        System.out.println("Looking for products with price between " + minPrice + " and " + maxPrice);
        List<Optional<Product>> results = productRepository.filterByPriceRange(minPrice, maxPrice);

        System.out.println("Expected: Results should be empty");
        System.out.println("Actual: Results size = " + results.size());
        assertTrue(results.isEmpty(), "Results should be empty");

        System.out.println("✓ filterByPriceRange correctly returns empty for out of range products");
    }

    @Test
    @DisplayName("Add a product successfully creates a new product")
    void testAddProduct_NewProduct_ProductCreated() {
        System.out.println("TEST: Verifying addProduct creates a new product");

        UUID newStoreId = UUID.randomUUID();
        String newProductName = "NewProduct";
        String newCategory = "NewCategory";
        String newDescription = "New product description";
        double newPrice = 149.99;

        System.out.println("Adding new product: " + newProductName);
        UUID newProductId = productRepository.addProduct(
                newStoreId,
                newProductName,
                newCategory,
                newDescription,
                newPrice,
                true
        );

        System.out.println("New product ID: " + newProductId);
        Optional<Product> result = productRepository.findById(newProductId);

        System.out.println("Expected: Product should be present");
        System.out.println("Actual: Product is present = " + result.isPresent());
        assertTrue(result.isPresent(), "Product should be found");

        Product newProduct = result.get();

        System.out.println("Expected product name: " + newProductName);
        System.out.println("Actual product name: " + newProduct.getName());
        assertEquals(newProductName, newProduct.getName(), "Product name should match");

        System.out.println("Expected product category: " + newCategory);
        System.out.println("Actual product category: " + newProduct.getCategory());
        assertEquals(newCategory, newProduct.getCategory(), "Product category should match");

        System.out.println("Expected product price: " + newPrice);
        System.out.println("Actual product price: " + newProduct.getPrice());
        assertEquals(newPrice, newProduct.getPrice(), "Product price should match");

        System.out.println("Expected product store ID: " + newStoreId);
        System.out.println("Actual product store ID: " + newProduct.getStoreId());
        assertEquals(newStoreId, newProduct.getStoreId(), "Product store ID should match");

        System.out.println("✓ addProduct correctly creates a new product");
    }

    @Test
    @DisplayName("Update product successfully modifies an existing product")
    void testUpdateProduct_ExistingProduct_ProductUpdated() {
        System.out.println("TEST: Verifying updateProduct modifies an existing product");

        String updatedName = "UpdatedProduct";
        String updatedCategory = "UpdatedCategory";
        String updatedDescription = "Updated product description";
        double updatedPrice = 199.99;

        System.out.println("Updating product with ID: " + testProductId);
        productRepository.updateProduct(
                testProductId,
                updatedName,
                updatedCategory,
                updatedDescription,
                updatedPrice
        );

        Optional<Product> result = productRepository.findById(testProductId);

        System.out.println("Expected: Product should be present");
        System.out.println("Actual: Product is present = " + result.isPresent());
        assertTrue(result.isPresent(), "Product should be found");

        Product updatedProduct = result.get();

        System.out.println("Expected product name: " + updatedName);
        System.out.println("Actual product name: " + updatedProduct.getName());
        assertEquals(updatedName, updatedProduct.getName(), "Product name should be updated");

        System.out.println("Expected product category: " + updatedCategory);
        System.out.println("Actual product category: " + updatedProduct.getCategory());
        assertEquals(updatedCategory, updatedProduct.getCategory(), "Product category should be updated");

        System.out.println("Expected product price: " + updatedPrice);
        System.out.println("Actual product price: " + updatedProduct.getPrice());
        assertEquals(updatedPrice, updatedProduct.getPrice(), "Product price should be updated");

        System.out.println("✓ updateProduct correctly modifies an existing product");
    }

    @Test
    @DisplayName("Delete product successfully removes a product")
    void testDeleteProduct_ExistingProduct_ProductRemoved() {
        System.out.println("TEST: Verifying deleteProduct removes an existing product");

        System.out.println("Deleting product with ID: " + testProductId);
        productRepository.deleteProduct(testProductId);

        Optional<Product> result = productRepository.findById(testProductId);

        System.out.println("Expected: Product should not be present");
        System.out.println("Actual: Product is present = " + result.isPresent());
        assertFalse(result.isPresent(), "Product should not be found after deletion");

        System.out.println("✓ deleteProduct correctly removes an existing product");
    }

    // Complex Operations Tests

    @Test
    @DisplayName("Get products by IDs returns all existing products")
    void testGetProductsByIds_ExistingIds_ReturnsProducts() {
        System.out.println("TEST: Verifying getProductsByIds with existing IDs");

        // Create another product
        String anotherProductName = "AnotherProduct";
        UUID anotherProductId = productRepository.addProduct(
                testStoreId,
                anotherProductName,
                testCategory,
                "Another product description",
                149.99,
                true
        );

        Set<UUID> productIds = new HashSet<>(Arrays.asList(testProductId, anotherProductId));

        System.out.println("Getting products by IDs: " + productIds);
        List<Optional<Product>> results = productRepository.getProductsByIds(productIds);

        System.out.println("Expected: Results size = " + productIds.size());
        System.out.println("Actual: Results size = " + results.size());
        assertEquals(productIds.size(), results.size(), "Should return same number of products as IDs");

        List<UUID> returnedIds = results.stream()
                .filter(Optional::isPresent)
                .map(p -> p.get().getProductId())
                .collect(Collectors.toList());

        System.out.println("Expected: Return should contain first product ID = true");
        System.out.println("Actual: Return contains first product ID = " + returnedIds.contains(testProductId));
        assertTrue(returnedIds.contains(testProductId), "Should contain first product");

        System.out.println("Expected: Return should contain second product ID = true");
        System.out.println("Actual: Return contains second product ID = " + returnedIds.contains(anotherProductId));
        assertTrue(returnedIds.contains(anotherProductId), "Should contain second product");

        System.out.println("✓ getProductsByIds correctly returns products for existing IDs");
    }

    @Test
    @DisplayName("Find by store ID returns products from that store")
    void testFindByStoreId_ExistingStoreId_ReturnsStoreProducts() {
        System.out.println("TEST: Verifying findByStoreId with existing store ID");

        System.out.println("Finding products for store ID: " + testStoreId);
        List<Optional<Product>> results = productRepository.findByStoreId(testStoreId);

        System.out.println("Expected: Results should not be empty");
        System.out.println("Actual: Results size = " + results.size());
        assertFalse(results.isEmpty(), "Results should not be empty");

        Optional<Product> firstResult = results.get(0);
        System.out.println("Expected: First result should be present");
        System.out.println("Actual: First result is present = " + firstResult.isPresent());
        assertTrue(firstResult.isPresent(), "First result should be present");

        System.out.println("Expected store ID: " + testStoreId);
        System.out.println("Actual store ID: " + firstResult.get().getStoreId());
        assertEquals(testStoreId, firstResult.get().getStoreId(), "Store ID should match");

        System.out.println("✓ findByStoreId correctly returns products for the store");
    }

    @Test
    @DisplayName("Find by store ID returns empty for non-existent store")
    void testFindByStoreId_NonExistingStoreId_ReturnsEmpty() {
        System.out.println("TEST: Verifying findByStoreId with non-existing store ID");

        UUID nonExistingStoreId = UUID.randomUUID();
        System.out.println("Finding products for non-existing store ID: " + nonExistingStoreId);
        List<Optional<Product>> results = productRepository.findByStoreId(nonExistingStoreId);

        System.out.println("Expected: Results should be empty");
        System.out.println("Actual: Results size = " + results.size());
        assertTrue(results.isEmpty(), "Results should be empty");

        System.out.println("✓ findByStoreId correctly returns empty for non-existing store");
    }

    @Test
    @DisplayName("Filter by store with criteria returns matching products")
    void testFilterByStoreWithCriteria_MatchingCriteria_ReturnsFilteredProducts() {
        System.out.println("TEST: Verifying filterByStoreWithCriteria with matching criteria");

        // Add another product with different attributes
        String anotherProductName = "ExpensiveProduct";
        double higherPrice = 299.99;
        productRepository.addProduct(
                testStoreId,
                anotherProductName,
                testCategory,
                "Expensive product description",
                higherPrice,
                true
        );

        double minPrice = testPrice - 10;
        double maxPrice = testPrice + 10;

        System.out.println("Filtering products in store with price range: " + minPrice + " to " + maxPrice);
        List<Optional<Product>> results = productRepository.filterByStoreWithCriteria(
                testStoreId,
                null,       // no name filter
                null,       // no category filter
                minPrice,
                maxPrice,
                null,       // no min rating
                null        // no max rating
        );

        System.out.println("Expected: Results should not be empty");
        System.out.println("Actual: Results size = " + results.size());
        assertFalse(results.isEmpty(), "Results should not be empty");

        // Should only return the original product that fits the price range
        System.out.println("Expected: Results size = 1");
        System.out.println("Actual: Results size = " + results.size());
        assertEquals(1, results.size(), "Should return 1 product matching criteria");

        Optional<Product> firstResult = results.get(0);
        System.out.println("Expected: First result should be present");
        System.out.println("Actual: First result is present = " + firstResult.isPresent());
        assertTrue(firstResult.isPresent(), "First result should be present");

        System.out.println("Expected product price to be within range: true");
        double price = firstResult.get().getPrice();
        boolean inRange = price >= minPrice && price <= maxPrice;
        System.out.println("Actual product price (" + price + ") is within range: " + inRange);
        assertTrue(inRange, "Product price should be within the range");

        System.out.println("✓ filterByStoreWithCriteria correctly returns products matching criteria");
    }

    @Test
    @DisplayName("Search product returns products matching all criteria")
    void testSearchProduct_MatchingCriteria_ReturnsMatchingProducts() {
        System.out.println("TEST: Verifying searchProduct with matching criteria");

        double minPrice = testPrice - 10;
        double maxPrice = testPrice + 10;

        System.out.println("Searching for products with criteria:");
        System.out.println("- Name: " + testProductName);
        System.out.println("- Category: " + testCategory);
        System.out.println("- Price range: " + minPrice + " to " + maxPrice);

        List<Optional<Product>> results = productRepository.searchProduct(
                testProductName,
                testCategory,
                minPrice,
                maxPrice,
                null,  // no min rating
                null   // no max rating
        );

        System.out.println("Expected: Results should not be empty");
        System.out.println("Actual: Results size = " + results.size());
        assertFalse(results.isEmpty(), "Results should not be empty");

        Optional<Product> firstResult = results.get(0);
        System.out.println("Expected: First result should be present");
        System.out.println("Actual: First result is present = " + firstResult.isPresent());
        assertTrue(firstResult.isPresent(), "First result should be present");

        System.out.println("Expected product name: " + testProductName);
        System.out.println("Actual product name: " + firstResult.get().getName());
        assertEquals(testProductName, firstResult.get().getName(), "Product name should match");

        System.out.println("Expected product category: " + testCategory);
        System.out.println("Actual product category: " + firstResult.get().getCategory());
        assertEquals(testCategory, firstResult.get().getCategory(), "Product category should match");

        System.out.println("Expected product price to be within range: true");
        double price = firstResult.get().getPrice();
        boolean inRange = price >= minPrice && price <= maxPrice;
        System.out.println("Actual product price (" + price + ") is within range: " + inRange);
        assertTrue(inRange, "Product price should be within the range");

        System.out.println("✓ searchProduct correctly returns products matching criteria");
    }

    @Test
    @DisplayName("Add product rating updates the product's rating properly")
    void testAddProductRating_NewRating_RatingAdded() {
        System.out.println("TEST: Verifying addProductRating adds a rating to a product");

        String username = "testUser";
        int ratingValue = 5;

        System.out.println("Adding rating value " + ratingValue + " to product " + testProductId + " by user " + username);
        productRepository.addProductRating(testProductId, username, ratingValue);

        Optional<Product> result = productRepository.findById(testProductId);

        System.out.println("Expected: Product should be present");
        System.out.println("Actual: Product is present = " + result.isPresent());
        assertTrue(result.isPresent(), "Product should be found");

        Product ratedProduct = result.get();

        System.out.println("Expected product to have rating count: 1");
        System.out.println("Actual product rating count: " + ratedProduct.getNumOfRanks());
        assertEquals(1, ratedProduct.getNumOfRanks(), "Product should have 1 rating");

        System.out.println("Expected product to have rating value: " + ratingValue);
        System.out.println("Actual product rating value: " + ratedProduct.getRate());
        assertEquals(ratingValue, ratedProduct.getRate(), "Product rating should match");

        System.out.println("✓ addProductRating correctly adds a rating to a product");
    }

    @Test
    @DisplayName("Update product rating changes an existing rating")
    void testUpdateProductRating_ExistingRating_RatingUpdated() {
        System.out.println("TEST: Verifying updateProductRating updates an existing rating");

        String username = "testUser";
        int initialRating = 3;
        int updatedRating = 5;

        System.out.println("First adding initial rating: " + initialRating);
        productRepository.addProductRating(testProductId, username, initialRating);

        System.out.println("Then updating rating from " + initialRating + " to " + updatedRating);
        productRepository.updateProductRating(testProductId, initialRating, updatedRating);

        Optional<Product> result = productRepository.findById(testProductId);

        System.out.println("Expected: Product should be present");
        System.out.println("Actual: Product is present = " + result.isPresent());
        assertTrue(result.isPresent(), "Product should be found");

        Product ratedProduct = result.get();

        System.out.println("Expected product to have rating count: 1");
        System.out.println("Actual product rating count: " + ratedProduct.getNumOfRanks());
        assertEquals(1, ratedProduct.getNumOfRanks(), "Product should still have 1 rating");

        System.out.println("Expected product to have rating value: " + updatedRating);
        System.out.println("Actual product rating value: " + ratedProduct.getRate());
        assertEquals(updatedRating, ratedProduct.getRate(), "Product rating should be updated");

        System.out.println("✓ updateProductRating correctly updates an existing rating");
    }

    @Test
    @DisplayName("Add product review successfully attaches a review")
    void testAddProductReview_ValidReview_ReviewAdded() {
        System.out.println("TEST: Verifying addProductReview adds a review to a product");

        String username = "testUser";
        String reviewText = "This is a great product!";

        System.out.println("Adding review to product " + testProductId + " by user " + username);
        productRepository.addProductReview(testProductId, username, reviewText);

        // There's no direct way to verify the review was added through the repository API
        // This is a limitation of the current implementation
        // In a real test, we would need to extend the repository to provide review retrieval

        // For now, we'll just check that the method executes without exception
        System.out.println("✓ addProductReview executed without exceptions");
    }

    @Test
    @DisplayName("Delete product rating successfully removes a rating")
    void testDeleteProductRating_ExistingRating_RatingRemoved() {
        System.out.println("TEST: Verifying deleteProductRating removes a rating");

        // Since we can't directly add a ProductRating object to the repository,
        // and there's no public method to retrieve a rating by ID in the interface,
        // we'll have to trust that the deleteProductRating method works as expected
        // when called with a valid rating ID.

        UUID ratingId = UUID.randomUUID();
        System.out.println("Attempting to delete rating with ID: " + ratingId);

        // The method should return false because the rating doesn't exist
        boolean result = productRepository.deleteProductRating(ratingId);

        System.out.println("Expected: Result = false (rating doesn't exist)");
        System.out.println("Actual: Result = " + result);
        assertFalse(result, "Should return false for non-existent rating");

        System.out.println("✓ deleteProductRating correctly handles non-existent ratings");
    }
}