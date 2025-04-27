package com.sadna_market.market.AcceptanceTests;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.Bridge;
import com.sadna_market.market.ApplicationLayer.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

public class UserTests {
    ObjectMapper objectMapper = new ObjectMapper();
    Bridge bridge = new Bridge();

    @BeforeEach
    void cleanUp(){
        // TODO
        // Bridge clear method
    }

    @Test
    void logoutTest() {
        // TODO
        // Bridge logout method
        Response response = bridge.logout();
        Assertions.assertNotEquals(null, response); // if response is null, logout did not work}
    }

    @Test
    void addProductToUserBasketTest() {
        // TODO
        // Bridge addProductToUserBasket method
        Response response = bridge.addProductToMemberBasket();
        Assertions.assertNotEquals(null, response); // if response is null, addProductToUserBasket did not work
        Assertions.assertFalse(response.isError()); // if error is true, addProductToUserBasket did not work
    }
    @Test
    void removeProductFromUserBasketTest() {
        // TODO
        // Bridge removeProductFromUserBasket method
        Response response = bridge.removeProductFromMemberBasket();
        Assertions.assertNotEquals(null, response); // if response is null, removeProductFromUserBasket did not work
        Assertions.assertFalse(response.isError()); // if error is true, removeProductFromUserBasket did not work
    }
    @Test
    void addUnavailableProductToUserBasketTest() {
        // TODO
        // Bridge addUnavailableProductToUserBasket method
        Response response = bridge.addUnavailableProductToMemberBasket();
        Assertions.assertNotEquals(null, response); // if response is null, addUnavailableProductToUserBasket did not work
        Assertions.assertTrue(response.isError()); // if error is false, addUnavailableProductToUserBasket did not work
    }
    @Test
    void removeUnavailableProductFromUserBasketTest() {
        // TODO
        // Bridge removeUnavailableProductFromUserBasket method
        Response response = bridge.removeUnavailableProductFromMemberBasket();
        Assertions.assertNotEquals(null, response); // if response is null, removeUnavailableProductFromUserBasket did not work
        Assertions.assertTrue(response.isError()); // if error is false, removeUnavailableProductFromUserBasket did not work
    }
    @Test
    void buyUserCartTest() {
        // TODO
        // Bridge buyUserCart method
        Response response = bridge.buyMemberCart();
        Assertions.assertNotEquals(null, response); // if response is null, buyUserCart did not work
        Assertions.assertFalse(response.isError()); // if error is true, buyUserCart did not work
    }
    @Test
    void getPurchaseHistoryTest() {
        // TODO
        // Bridge getPurchaseHistory method
        Response response = bridge.getPurchaseHistory();
        Assertions.assertNotEquals(null, response); // if response is null, getPurchaseHistory did not work
        Assertions.assertFalse(response.isError()); // if error is true, getPurchaseHistory did not work
    }
    @Test
    void createStoreTest() {
        // TODO
        // Bridge openStore method
        Response response = bridge.createStore();
        Assertions.assertNotEquals(null, response); // if response is null, openStore did not work
        Assertions.assertFalse(response.isError()); // if error is true, openStore did not work
    }
}
