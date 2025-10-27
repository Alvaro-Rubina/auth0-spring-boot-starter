package com.example.auth0springbootstarter.utils;

import java.util.HashMap;
import java.util.Map;

public final class Constants {

    public static final String USER_ROLE_NAME = "USER";
    public static final String ADMIN_ROLE_NAME = "ADMIN";
    public static final String OWNER_ROLE_NAME = "OWNER";

    public static final Map<String, String> ROLES = new HashMap<>() {{
        put(ADMIN_ROLE_NAME, "Rol con permisos extendidos para administradores.");
        put(USER_ROLE_NAME, "Rol con permisos limitados para usuarios.");
        put(OWNER_ROLE_NAME, "Rol con permisos absolutos para propietarios");
    }};
}