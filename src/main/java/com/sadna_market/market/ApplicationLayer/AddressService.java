package com.sadna_market.market.ApplicationLayer;

import com.sadna_market.market.ApplicationLayer.DTOs.AddressDTO;
import com.sadna_market.market.ApplicationLayer.Requests.AddressRequest;
import com.sadna_market.market.DomainLayer.*;
import com.sadna_market.market.InfrastructureLayer.Authentication.AuthenticationAdapter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {
    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);

    private final IAddressRepository addressRepository;
    private final IUserRepository userRepository;
    private final AuthenticationAdapter authentication;

    /**
     * Add a new address for a user
     */
    public Response<AddressDTO> addAddress(String username, String token, AddressRequest request) {
        try {
            logger.info("Adding address for user: {}", username);
            authentication.validateToken(username, token);

            // Validate request
            if (!request.isValidRequest()) {
                return Response.error("Invalid address data provided");
            }

            // Verify user exists
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

            // Create address
            Address address = new Address(
                    username,
                    request.getFullName(),
                    request.getAddressLine1(),
                    request.getAddressLine2(),
                    request.getCity(),
                    request.getState(),
                    request.getPostalCode(),
                    request.getCountry(),
                    request.getPhoneNumber(),
                    request.getLabel() != null ? request.getLabel() : "Home"
            );

            // If this is the user's first address or explicitly set as default
            List<Address> existingAddresses = addressRepository.findByUsername(username);
            if (existingAddresses.isEmpty() || request.isDefault()) {
                address.setDefault(true);
                // Unset other defaults if this is being set as default
                if (request.isDefault()) {
                    addressRepository.setAsDefault(username, address.getAddressId());
                }
            }

            // Save address
            Address savedAddress = addressRepository.save(address);

            // Add to user's address list
            user.addAddressId(savedAddress.getAddressId());
            userRepository.update(user);

            logger.info("Address added successfully: {}", savedAddress.getAddressId());
            return Response.success(new AddressDTO(savedAddress));

        } catch (Exception e) {
            logger.error("Error adding address: {}", e.getMessage(), e);
            return Response.error("Failed to add address: " + e.getMessage());
        }
    }

    /**
     * Get all addresses for a user
     */
    public Response<List<AddressDTO>> getUserAddresses(String username, String token) {
        try {
            logger.info("Getting addresses for user: {}", username);
            authentication.validateToken(username, token);

            List<Address> addresses = addressRepository.findByUsername(username);
            List<AddressDTO> addressDTOs = addresses.stream()
                    .map(AddressDTO::new)
                    .collect(Collectors.toList());

            logger.info("Retrieved {} addresses for user: {}", addressDTOs.size(), username);
            return Response.success(addressDTOs);

        } catch (Exception e) {
            logger.error("Error getting user addresses: {}", e.getMessage(), e);
            return Response.error("Failed to get addresses: " + e.getMessage());
        }
    }

    /**
     * Update an existing address
     */
    public Response<AddressDTO> updateAddress(String username, String token, UUID addressId, AddressRequest request) {
        try {
            logger.info("Updating address {} for user: {}", addressId, username);
            authentication.validateToken(username, token);

            // Validate request
            if (!request.isValidRequest()) {
                return Response.error("Invalid address data provided");
            }

            // Verify address exists and belongs to user
            Address existingAddress = addressRepository.findById(addressId)
                    .orElseThrow(() -> new IllegalArgumentException("Address not found: " + addressId));

            if (!existingAddress.getUsername().equals(username)) {
                throw new IllegalStateException("Address does not belong to user");
            }

            // Update address fields
            existingAddress.setFullName(request.getFullName());
            existingAddress.setAddressLine1(request.getAddressLine1());
            existingAddress.setAddressLine2(request.getAddressLine2());
            existingAddress.setCity(request.getCity());
            existingAddress.setState(request.getState());
            existingAddress.setPostalCode(request.getPostalCode());
            existingAddress.setCountry(request.getCountry());
            existingAddress.setPhoneNumber(request.getPhoneNumber());
            existingAddress.setLabel(request.getLabel() != null ? request.getLabel() : "Home");

            // Handle default setting
            if (request.isDefault() && !existingAddress.isDefault()) {
                addressRepository.setAsDefault(username, addressId);
                existingAddress.setDefault(true);
            }

            // Save updated address
            Address updatedAddress = addressRepository.update(existingAddress);

            logger.info("Address updated successfully: {}", addressId);
            return Response.success(new AddressDTO(updatedAddress));

        } catch (Exception e) {
            logger.error("Error updating address: {}", e.getMessage(), e);
            return Response.error("Failed to update address: " + e.getMessage());
        }
    }

    /**
     * Delete an address
     */
    public Response<String> deleteAddress(String username, String token, UUID addressId) {
        try {
            logger.info("Deleting address {} for user: {}", addressId, username);
            authentication.validateToken(username, token);

            // Verify address exists and belongs to user
            if (!addressRepository.isAddressOwnedByUser(username, addressId)) {
                return Response.error("Address not found or does not belong to user");
            }

            // Check if this is the default address
            Address address = addressRepository.findById(addressId).orElse(null);
            boolean wasDefault = address != null && address.isDefault();

            // Delete the address
            boolean deleted = addressRepository.deleteById(addressId);

            if (deleted) {
                // Remove from user's address list
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    user.removeAddressId(addressId);
                    userRepository.update(user);
                }

                // If this was the default address, set another as default
                if (wasDefault) {
                    List<Address> remainingAddresses = addressRepository.findByUsername(username);
                    if (!remainingAddresses.isEmpty()) {
                        addressRepository.setAsDefault(username, remainingAddresses.get(0).getAddressId());
                    }
                }

                logger.info("Address deleted successfully: {}", addressId);
                return Response.success("Address deleted successfully");
            } else {
                return Response.error("Failed to delete address");
            }

        } catch (Exception e) {
            logger.error("Error deleting address: {}", e.getMessage(), e);
            return Response.error("Failed to delete address: " + e.getMessage());
        }
    }

    /**
     * Set an address as default
     */
    public Response<String> setDefaultAddress(String username, String token, UUID addressId) {
        try {
            logger.info("Setting address {} as default for user: {}", addressId, username);
            authentication.validateToken(username, token);

            // Verify address exists and belongs to user
            if (!addressRepository.isAddressOwnedByUser(username, addressId)) {
                return Response.error("Address not found or does not belong to user");
            }

            boolean success = addressRepository.setAsDefault(username, addressId);

            if (success) {
                logger.info("Address set as default successfully: {}", addressId);
                return Response.success("Default address updated successfully");
            } else {
                return Response.error("Failed to set default address");
            }

        } catch (Exception e) {
            logger.error("Error setting default address: {}", e.getMessage(), e);
            return Response.error("Failed to set default address: " + e.getMessage());
        }
    }

    /**
     * Get user's default address
     */
    public Response<AddressDTO> getDefaultAddress(String username, String token) {
        try {
            logger.info("Getting default address for user: {}", username);
            authentication.validateToken(username, token);

            Address defaultAddress = addressRepository.findDefaultByUsername(username).orElse(null);

            if (defaultAddress != null) {
                return Response.success(new AddressDTO(defaultAddress));
            } else {
                return Response.error("No default address found");
            }

        } catch (Exception e) {
            logger.error("Error getting default address: {}", e.getMessage(), e);
            return Response.error("Failed to get default address: " + e.getMessage());
        }
    }
}