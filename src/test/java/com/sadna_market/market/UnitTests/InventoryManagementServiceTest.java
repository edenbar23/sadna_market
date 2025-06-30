package com.sadna_market.market.UnitTests;

import com.sadna_market.market.DomainLayer.DomainServices.InventoryManagementService;
import com.sadna_market.market.InfrastructureLayer.InMemoryRepos.InMemoryProductRepository;
import com.sadna_market.market.InfrastructureLayer.InMemoryRepos.InMemoryStoreRepository;
import com.sadna_market.market.InfrastructureLayer.InMemoryRepos.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Inventory Management Service Tests")
class InventoryManagementServiceTest {
    private InventoryManagementService inventoryService;
    private InMemoryStoreRepository storeRepository;
    private InMemoryProductRepository productRepository;
    private InMemoryUserRepository userRepository;

    private UUID storeId;
    private final String ownerUsername = "owner";

    @BeforeEach
    void setUp() {
        storeRepository = new InMemoryStoreRepository();
        productRepository = new InMemoryProductRepository();
        userRepository = new InMemoryUserRepository();
        inventoryService = new InventoryManagementService(storeRepository, productRepository, userRepository);
        storeId = storeRepository.createStore(ownerUsername, "TestStore", "Address", "email@test.com", "123456789");
    }

    @Test
    @DisplayName("Adding product with duplicate name should fail")
    void addProductWithDuplicateNameFails() {
        inventoryService.addProductToStore(ownerUsername, storeId, "Prod", "Category", "desc", 10.0, 5);

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.addProductToStore(ownerUsername, storeId, "Prod", "Other", "desc2", 15.0, 3)
        );
    }
}
