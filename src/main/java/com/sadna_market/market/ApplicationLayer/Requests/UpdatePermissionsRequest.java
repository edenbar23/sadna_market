package com.sadna_market.market.ApplicationLayer.Requests;

import java.util.List;

public class UpdatePermissionsRequest {
    private List<Integer> permissions;

    public UpdatePermissionsRequest(List<Integer> permissions) {
        this.permissions = permissions;
    }

    public List<Integer> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Integer> permissions) {
        this.permissions = permissions;
    }
}
