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
public class StoreOwnerTests {
    ObjectMapper objectMapper = new ObjectMapper();
    Bridge bridge = new Bridge();

    @BeforeEach
    void cleanUp(){
        // TODO
        // Bridge clear method
    }
    @Test
    void closeStoreTest(){
        Response response = bridge.closeStore();
        Assertions.assertNotEquals(null, response); // if response is null, closeStore did not work
        Assertions.assertFalse(response.isError()); // if error is true, closeStore did not work
        //TODO
        // decide what is the JSON value in case of store owner closing store
    }
    @Test
    void reOpenStoreTest(){
        Response response = bridge.reOpenStore();
        Assertions.assertNotEquals(null, response); // if response is null, reOpenStore did not work
        Assertions.assertFalse(response.isError()); // if error is true, reOpenStore did not work
        //TODO
        // decide what is the JSON value in case of store owner reopening store
    }
    @Test
    void addProductToStoreTest(){
        Response response = bridge.addProductToStore();
        Assertions.assertNotEquals(null, response); // if response is null, addProductToStore did not work
        Assertions.assertFalse(response.isError()); // if error is true, addProductToStore did not work
        //TODO
        // decide what is the JSON value in case of store owner adding product to store
    }
    @Test
    void removeProductFromStoreTest(){
        Response response = bridge.removeProductFromStore();
        Assertions.assertNotEquals(null, response); // if response is null, removeProductFromStore did not work
        Assertions.assertFalse(response.isError()); // if error is true, removeProductFromStore did not work
        //TODO
        // decide what is the JSON value in case of store owner removing product from store
    }
    @Test
    void editProductDetailsTest(){
        Response response = bridge.editProductDetails();
        Assertions.assertNotEquals(null, response); // if response is null, editProductDetails did not work
        Assertions.assertFalse(response.isError()); // if error is true, editProductDetails did not work
        //TODO
        // decide what is the JSON value in case of store owner editing product details
    }
    @Test
    void getProductsListTest(){
        Response response = bridge.getProductsList();
        Assertions.assertNotEquals(null, response); // if response is null, getProductsList did not work
        Assertions.assertFalse(response.isError()); // if error is true, getProductsList did not work
        //TODO
        // decide what is the JSON value in case of store owner getting products list
    }
    @Test
    void getRolesTest(){
        Response response = bridge.getRoles();
        Assertions.assertNotEquals(null, response); // if response is null, getRoles did not work
        Assertions.assertFalse(response.isError()); // if error is true, getRoles did not work
        //TODO
        // decide what is the JSON value in case of store owner getting roles
    }
    @Test
    void appointManagerTest(){
        Response response = bridge.appointManager();
        Assertions.assertNotEquals(null, response); // if response is null, appointManager did not work
        Assertions.assertFalse(response.isError()); // if error is true, appointManager did not work
        //TODO
        // decide what is the JSON value in case of store owner appointing manager
    }
    @Test
    void fireManagerTest(){
        Response response = bridge.fireManager();
        Assertions.assertNotEquals(null, response); // if response is null, fireManager did not work
        Assertions.assertFalse(response.isError()); // if error is true, fireManager did not work
        //TODO
        // decide what is the JSON value in case of store owner firing manager
    }
    @Test
    void appointOwnerTest(){
        Response response = bridge.appointOwner();
        Assertions.assertNotEquals(null, response); // if response is null, appointStoreOwner did not work
        Assertions.assertFalse(response.isError()); // if error is true, appointStoreOwner did not work
        //TODO
        // decide what is the JSON value in case of store owner appointing store owner
    }
    @Test
    void fireOwnerTest(){
        Response response = bridge.fireOwner();
        Assertions.assertNotEquals(null, response); // if response is null, fireStoreOwner did not work
        Assertions.assertFalse(response.isError()); // if error is true, fireStoreOwner did not work
        //TODO
        // decide what is the JSON value in case of store owner firing store owner
    }
    @Test
    void editManagerPermissionsTest(){
        Response response = bridge.editManagerPermissions();
        Assertions.assertNotEquals(null, response); // if response is null, editManagerPermissions did not work
        Assertions.assertFalse(response.isError()); // if error is true, editManagerPermissions did not work
        //TODO
        // decide what is the JSON value in case of store owner editing manager permissions
    }
    @Test
    void setStoreDiscountPolicyTest(){
        Response response = bridge.setStoreDiscountPolicy();
        Assertions.assertNotEquals(null, response); // if response is null, setStoreDiscountPolicy did not work
        Assertions.assertFalse(response.isError()); // if error is true, setStoreDiscountPolicy did not work
        //TODO
        // decide what is the JSON value in case of store owner setting store discount policy
    }
    @Test
    void setPurchasePolicyTest(){
        Response response = bridge.setPurchasePolicy();
        Assertions.assertNotEquals(null, response); // if response is null, setPurchasePolicy did not work
        Assertions.assertFalse(response.isError()); // if error is true, setPurchasePolicy did not work
        //TODO
        // decide what is the JSON value in case of store owner setting purchase policy
    }
    @Test
    void getStoreHistoryTest(){
        Response response = bridge.getStoreHistory();
        Assertions.assertNotEquals(null, response); // if response is null, getStoreHistory did not work
        Assertions.assertFalse(response.isError()); // if error is true, getStoreHistory did not work
        //TODO
        // decide what is the JSON value in case of store owner getting store history
    }
    @Test
    void giveUpOwnerShipTest(){
        Response response = bridge.giveUpOwnerShip();
        Assertions.assertNotEquals(null, response); // if response is null, giveUpOwnerShip did not work
        Assertions.assertFalse(response.isError()); // if error is true, giveUpOwnerShip did not work
        //TODO
        // decide what is the JSON value in case of store owner giving up ownership
    }
}
