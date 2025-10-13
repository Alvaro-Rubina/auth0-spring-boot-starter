package com.example.auth0springbootstarter.utils;

import java.util.HashMap;
import java.util.Map;

public final class Constants {

    public static final String USER_ROL_NAME = "USUARIO";
    public static final String ADMIN_ROL_NAME = "ADMIN";

    public static final Map<String, String> ROLES = new HashMap<>() {{
        put(ADMIN_ROL_NAME, "Rol con permisos extendidos para administradores.");
        put(USER_ROL_NAME, "Rol con permisos limitados para usuarios.");
    }};
}