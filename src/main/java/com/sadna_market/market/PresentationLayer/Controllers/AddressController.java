package com.sadna_market.market.PresentationLayer.Controllers;

import com.sadna_market.market.ApplicationLayer.AddressService;
import com.sadna_market.market.ApplicationLayer.DTOs.AddressDTO;
import com.sadna_market.market.ApplicationLayer.Requests.AddressRequest;
import com.sadna_market.market.ApplicationLayer.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{username}/addresses")
@CrossOrigin(origins = "*")
public class AddressController {
    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);

    private final AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * Add a new address for a user
     */
    @PostMapping
    public ResponseEntity<Response<AddressDTO>> addAddress(
            @PathVariable String username,
            @RequestHeader("Authorization") String token,
            @RequestBody AddressRequest request) {

        logger.info("Adding address for user: {}", username);

        Response<AddressDTO> response = addressService.addAddress(username, token, request);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all addresses for a user
     */
    @GetMapping
    public ResponseEntity<Response<List<AddressDTO>>> getUserAddresses(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        logger.info("Getting addresses for user: {}", username);

        Response<List<AddressDTO>> response = addressService.getUserAddresses(username, token);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing address
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<Response<AddressDTO>> updateAddress(
            @PathVariable String username,
            @PathVariable UUID addressId,
            @RequestHeader("Authorization") String token,
            @RequestBody AddressRequest request) {

        logger.info("Updating address {} for user: {}", addressId, username);

        Response<AddressDTO> response = addressService.updateAddress(username, token, addressId, request);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Delete an address
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Response<String>> deleteAddress(
            @PathVariable String username,
            @PathVariable UUID addressId,
            @RequestHeader("Authorization") String token) {

        logger.info("Deleting address {} for user: {}", addressId, username);

        Response<String> response = addressService.deleteAddress(username, token, addressId);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Set an address as default
     */
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<Response<String>> setDefaultAddress(
            @PathVariable String username,
            @PathVariable UUID addressId,
            @RequestHeader("Authorization") String token) {

        logger.info("Setting address {} as default for user: {}", addressId, username);

        Response<String> response = addressService.setDefaultAddress(username, token, addressId);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get user's default address
     */
    @GetMapping("/default")
    public ResponseEntity<Response<AddressDTO>> getDefaultAddress(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {

        logger.info("Getting default address for user: {}", username);

        Response<AddressDTO> response = addressService.getDefaultAddress(username, token);

        if (response.isError()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(response);
    }
}