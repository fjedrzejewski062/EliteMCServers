package com.example.elitemcservers.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @GetMapping("/debug-auth")
    public String debugAuth(Authentication authentication) {
        if (authentication == null) {
            return "Not authenticated";
        }
        return "Authenticated as: " + authentication.getName() + ", authorities: " + authentication.getAuthorities();
    }
}