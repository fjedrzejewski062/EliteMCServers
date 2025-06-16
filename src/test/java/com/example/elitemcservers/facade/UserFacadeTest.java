package com.example.elitemcservers.facade;

import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserFacadeTest {

    @Mock
    private UserService userService;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserFacade userFacade;

    private final String adminEmail = "admin@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userFacade = new UserFacade(userService, passwordEncoder, adminEmail);
    }

    @Test
    @DisplayName("registerUser - success when valid data and non-admin email")
    void registerUser_Success() {
        User user = new User();
        user.setUsername("user1");
        user.setEmail("user1@example.com");
        user.setPassword("pass");

        when(userService.findByUsername("user1")).thenReturn(Optional.empty());
        when(userService.findByEmail("user1@example.com")).thenReturn(Optional.empty());
        when(userService.register(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userFacade.registerUser(user, "pass", "accepted");
        assertEquals("user1", registered.getUsername());
        assertEquals("USER", registered.getRole()); // poprawione na "USER"

        verify(userService).register(user);
    }


    @Test
    @DisplayName("registerUser - sets ADMIN role when email equals adminEmail")
    void registerUser_AdminEmail_SetsRole() {
        User user = new User();
        user.setUsername("adminuser");
        user.setEmail(adminEmail);
        user.setPassword("pass");

        when(userService.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userService.register(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userFacade.registerUser(user, "pass", "accepted");
        assertEquals("ADMIN", registered.getRole());
        verify(userService).register(user);
    }

    @Test
    @DisplayName("registerUser - throws when passwords do not match")
    void registerUser_PasswordMismatch_Throws() {
        User user = new User();
        user.setPassword("pass1");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userFacade.registerUser(user, "pass2", "accepted"));
        assertEquals("Passwords do not match", ex.getMessage());
    }

    @Test
    @DisplayName("registerUser - throws when terms not accepted")
    void registerUser_TermsNotAccepted_Throws() {
        User user = new User();
        user.setPassword("pass");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userFacade.registerUser(user, "pass", null));
        assertEquals("You must agree to the terms and conditions", ex.getMessage());
    }

    @Test
    @DisplayName("registerUser - throws when username taken")
    void registerUser_UsernameTaken_Throws() {
        User user = new User();
        user.setUsername("user1");
        user.setPassword("pass");

        when(userService.findByUsername("user1")).thenReturn(Optional.of(new User()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userFacade.registerUser(user, "pass", "accepted"));
        assertEquals("Username is already taken", ex.getMessage());
    }

    @Test
    @DisplayName("registerUser - throws when email taken")
    void registerUser_EmailTaken_Throws() {
        User user = new User();
        user.setUsername("user1");
        user.setEmail("email@example.com");
        user.setPassword("pass");

        when(userService.findByUsername("user1")).thenReturn(Optional.empty());
        when(userService.findByEmail("email@example.com")).thenReturn(Optional.of(new User()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userFacade.registerUser(user, "pass", "accepted"));
        assertEquals("Email is already registered", ex.getMessage());
    }

    @Test
    @DisplayName("update - delegates to userService.update")
    void update_Delegates() {
        User user = new User();
        when(userService.update(user)).thenReturn(user);

        User updated = userFacade.update(user);
        assertSame(user, updated);
        verify(userService).update(user);
    }

    @Test
    @DisplayName("softDelete - delegates to userService.softDelete")
    void softDelete_Delegates() {
        User user = new User();

        userFacade.softDelete(user);
        verify(userService).softDelete(user);
    }

    @Test
    @DisplayName("findByEmail - delegates to userService.findByEmail")
    void findByEmail_Delegates() {
        String email = "test@example.com";
        when(userService.findByEmail(email)).thenReturn(Optional.of(new User()));

        Optional<User> result = userFacade.findByEmail(email);
        assertTrue(result.isPresent());
        verify(userService).findByEmail(email);
    }

    @Test
    @DisplayName("findById - delegates to userService.findById")
    void findById_Delegates() {
        User user = new User();
        when(userService.findById(1L)).thenReturn(user);

        User result = userFacade.findById(1L);
        assertSame(user, result);
        verify(userService).findById(1L);
    }

    @Test
    @DisplayName("findFilteredUsers - delegates to userService.findFilteredUsers")
    void findFilteredUsers_Delegates() {
        Page<User> page = new PageImpl<>(List.of(new User()));
        when(userService.findFilteredUsers(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        Page<User> result = userFacade.findFilteredUsers(null,null,null,null,null,
                null,null,null,null, Pageable.unpaged());

        assertEquals(page, result);
        verify(userService).findFilteredUsers(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("getAuthenticatedUser - delegates to userService.getAuthenticatedUser")
    void getAuthenticatedUser_Delegates() {
        Authentication authentication = mock(Authentication.class);
        Optional<User> userOpt = Optional.of(new User());
        when(userService.getAuthenticatedUser(authentication)).thenReturn(userOpt);

        Optional<User> result = userFacade.getAuthenticatedUser(authentication);
        assertEquals(userOpt, result);
        verify(userService).getAuthenticatedUser(authentication);
    }
}
