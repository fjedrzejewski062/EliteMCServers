package com.example.elitemcservers.controller;

import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.facade.UserFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // Wyłącz filtry Spring Security, jeśli potrzebujesz
@Import(UserControllerTest.MockConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserFacade userFacade;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public UserFacade userFacade() {
            return Mockito.mock(UserFacade.class);
        }
    }

    @Test
    @DisplayName("GET /register - should show registration form")
    public void testShowRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("POST /register - valid registration")
    public void testRegisterUserSuccess() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setEmail("testuser@example.com");

        when(userFacade.registerUser(any(User.class), anyString(), anyString()))
                .thenReturn(mockUser);

        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("email", "testuser@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("terms", "on"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(userFacade).registerUser(any(User.class), eq("password123"), eq("on"));
    }

    @Test
    @DisplayName("POST /register - invalid registration (throws exception)")
    public void testRegisterUser_Invalid() throws Exception {
        doThrow(new IllegalArgumentException("Passwords do not match"))
                .when(userFacade).registerUser(any(User.class), anyString(), anyString());

        mockMvc.perform(post("/register")
                        .param("username", "testuser")
                        .param("email", "testuser@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "wrongPassword")
                        .param("terms", "on"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @DisplayName("GET /login - shows login page")
    public void testShowLoginForm() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET /myprofile - authenticated user")
    @WithMockUser(username = "test@example.com")
    public void testShowProfile_Authenticated() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        when(userFacade.getAuthenticatedUser(any(Authentication.class))).thenReturn(Optional.of(user));

        mockMvc.perform(get("/myprofile"))
                .andExpect(status().isOk())
                .andExpect(view().name("myProfile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /myprofile - unauthenticated")
    public void testShowProfile_Unauthenticated() throws Exception {
        when(userFacade.getAuthenticatedUser(any(Authentication.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/myprofile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("POST /deleteaccount - deletes account and redirects")
    @WithMockUser(username = "test@example.com")
    public void testDeleteAccount() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");

        when(userFacade.getAuthenticatedUser(any(Authentication.class))).thenReturn(Optional.of(user));
        doNothing().when(userFacade).softDelete(any(User.class));

        mockMvc.perform(post("/deleteaccount"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logout"));

        verify(userFacade).softDelete(argThat(u -> u.getEmail().equals("test@example.com")));
    }
}
