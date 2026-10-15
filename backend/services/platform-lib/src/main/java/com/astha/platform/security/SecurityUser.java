package com.astha.platform.security;

import java.util.Set;

public record SecurityUser(String userId, String username, Set<UserRole> roles) {
    public boolean hasRole(UserRole role) {
        return roles != null && roles.contains(role);
    }
}
