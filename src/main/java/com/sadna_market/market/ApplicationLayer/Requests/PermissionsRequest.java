package com.sadna_market.market.ApplicationLayer.Requests;

import com.sadna_market.market.DomainLayer.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionsRequest {
    private Set<Permission> permissions;
}