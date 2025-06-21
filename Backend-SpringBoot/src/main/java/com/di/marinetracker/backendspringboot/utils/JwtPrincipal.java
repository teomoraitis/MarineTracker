package com.di.marinetracker.backendspringboot.utils;

import java.security.Principal;

public class JwtPrincipal implements Principal {
    private final String jwt;
    private final String name;

    public JwtPrincipal(String jwt, String name) {
        this.jwt = jwt;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getJwt() {
        return jwt;
    }
}
