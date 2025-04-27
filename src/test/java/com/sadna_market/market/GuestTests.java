package com.sadna_market.market;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna_market.market.ApplicationLayer.Bridge;
import com.sadna_market.market.ApplicationLayer.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

public class GuestTests {
    ObjectMapper objectMapper = new ObjectMapper();
    Bridge bridge = new Bridge();

    @BeforeEach
    void cleanUp(){
        // TODO
        // Bridge clear method
    }

    @Test
    void signUpGuestTest(){
        Response response = bridge.signUp();
        Assertions.assertNotEquals(null, response); // if response is null, signUp did not work
        Assertions.assertFalse(response.isError()); // if error is true, signUp did not work
        //TODO
        // decide what is the JSON value in case of guest signing up
    }

    @Test
    void loginGuestTest(){
        Response response = bridge.login();
        Assertions.assertNotEquals(null, response); // if response is null, login did not work
        Assertions.assertFalse(response.isError()); // if error is true, login did not work
        //TODO
        // decide what is the JSON value in case of guest logging in
    }

    @Test
    void addProductToGuestBasketTest(){
        Response response = bridge.addProductToGuestBasket();
        Assertions.assertNotEquals(null, response); // if response is null, addProductToGuestBasket did not work
        Assertions.assertFalse(response.isError()); // if error is true, addProductToGuestBasket did not work
        //TODO
        // decide what is the JSON value in case of guest adding product to basket
    }
    @Test
    void removeProductFromGuestBasketTest(){
        Response response = bridge.removeProductFromGuestBasket();
        Assertions.assertNotEquals(null, response); // if response is null, removeProductFromGuestBasket did not work
        Assertions.assertFalse(response.isError()); // if error is true, removeProductFromGuestBasket did not work
        //TODO
        // decide what is the JSON value in case of guest removing product from basket
    }
    @Test
    void addUnavailableProductToGuestBasketTest(){
        Response response = bridge.addUnavailableProductToGuestBasket();
        Assertions.assertNotEquals(null, response); // if response is null, addUnavailableProductToGuestBasket did not work
        Assertions.assertTrue(response.isError()); // if error is false, addUnavailableProductToGuestBasket did not work
        //TODO
        // decide what is the JSON value in case of guest adding unavailable product to basket
    }

    @Test
    void removeUnavailableProductFromGuestBasketTest(){
        Response response = bridge.removeUnavailableProductFromGuestBasket();
        Assertions.assertNotEquals(null, response); // if response is null, removeUnavailableProductFromGuestBasket did not work
        Assertions.assertTrue(response.isError()); // if error is false, removeUnavailableProductFromGuestBasket did not work
        //TODO
        // decide what is the JSON value in case of guest removing unavailable product from basket
    }
}
