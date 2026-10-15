package com.astha.platform.security;

public class SecurityContext {
    private static final ThreadLocal<SecurityUser> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUser(SecurityUser user) {
        CURRENT_USER.set(user);
    }

    public static SecurityUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
