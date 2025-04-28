package com.sadna_market.market.ApplicationLayer.Requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import com.sadna_market.market.DomainLayer.Permission;

// 1. RegisterRequest - Complex user data
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String userName;
    private String password;
    private String email;
    private String firstName;
    private String lastName;

    public RegisterRequest(String username, String password, String s, String store, String owner) {

    }
}
