package com.example.elitemcservers.facade;

import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserFacade {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;

    public UserFacade(UserService userService, PasswordEncoder passwordEncoder, @Value("${admin.email}") String adminEmail) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
    }

    public User registerUser(User user, String confirmPassword, String terms) {
        if (!user.getPassword().equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (terms == null) {
            throw new IllegalArgumentException("You must agree to the terms and conditions");
        }

        if (userService.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userService.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Przypisanie roli admina
        if (adminEmail != null && adminEmail.equalsIgnoreCase(user.getEmail())) {
            user.setRole("ADMIN");
        }

        return userService.register(user);
    }

    public Optional<User> findByEmail(String email) {
        return userService.findByEmail(email);
    }

    public void deleteUserAccount(User user) {
        userService.softDelete(user);
    }
}