package com.example.elitemcservers.service;

import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should load user with USER role")
    public void testLoadUserByUsername_UserExists() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");
        user.setRole("USER");
        user.setBanned(false);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("user@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("USER");
    }

    @Test
    @DisplayName("Should load user with ADMIN role")
    public void testLoadUserByUsername_AdminExists() {
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPassword("adminPassword");
        admin.setRole("ADMIN");
        admin.setBanned(false);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin@example.com");
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ADMIN");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    public void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("notfound@example.com"));
    }

    @Test
    @DisplayName("Should load banned user")
    public void testLoadUserByUsername_BannedUser() {
        User banned = new User();
        banned.setEmail("banned@example.com");
        banned.setPassword("bannedPassword");
        banned.setRole("USER");
        banned.setBanned(true);

        when(userRepository.findByEmail("banned@example.com")).thenReturn(Optional.of(banned));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("banned@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("banned@example.com");
    }
}
