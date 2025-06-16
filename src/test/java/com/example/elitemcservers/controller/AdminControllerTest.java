package com.example.elitemcservers.controller;

import com.example.elitemcservers.admin.AdminController;
import com.example.elitemcservers.entity.Comment;
import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerStatus;
import com.example.elitemcservers.enums.ServerVersion;
import com.example.elitemcservers.facade.CommentFacade;
import com.example.elitemcservers.facade.ServerFacade;
import com.example.elitemcservers.facade.UserFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminControllerTest {

    private MockMvc mockMvc;

    private ServerFacade serverFacade;
    private UserFacade userFacade;
    private CommentFacade commentFacade;

    private AdminController adminController;

    @BeforeEach
    void setup() {
        serverFacade = Mockito.mock(ServerFacade.class);
        userFacade = Mockito.mock(UserFacade.class);
        commentFacade = Mockito.mock(CommentFacade.class);

        adminController = new AdminController(serverFacade, userFacade, commentFacade);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    private Server mockServer() {
        Server server = new Server();
        server.setId(1L);
        server.setCreatedBy(mockUser());
        server.setServerName("Test Server");
        server.setIpAddress("127.0.0.1");
        server.setVersion(ServerVersion.V1_19_0);
        server.setMode(ServerMode.SURVIVAL);
        server.setDescription("Test server description.");
        server.setUpVotes(5);
        server.setDownVotes(2);
        server.setScore(3);
        server.setStatus(ServerStatus.APPROVED);
        server.setCreatedAt(LocalDateTime.now().minusDays(2));
        server.setUpdatedAt(LocalDateTime.now());
        return server;
    }

    private User mockUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("adminuser");
        user.setEmail("admin@example.com");
        user.setPassword("adminpass123");
        user.setRole("ADMIN");
        user.setProfileImage("/img/admin.png");
        user.setBanned(false);
        user.setDeleted(false);
        user.setRegistrationDate(LocalDateTime.now().minusDays(30));
        user.setLastLogin(LocalDateTime.now());
        return user;
    }

    private Comment mockComment() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("This is a test comment.");
        comment.setCreationDate(LocalDateTime.now().minusHours(1));
        comment.setServer(mockServer());
        comment.setCreatedBy(mockUser());
        return comment;
    }

    @Test
    @DisplayName("GET /admin/dashboard - displays admin dashboard")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminDashboard.html"));
    }

    @Test
    @DisplayName("GET /admin/servers - displays filtered server list")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testAdminServerList() throws Exception {
        Server server = mockServer();
        Page<Server> serverPage = new PageImpl<>(List.of(server));
        when(serverFacade.findFilteredServersAdmin(
                any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(),
                any()
        )).thenReturn(serverPage);

        mockMvc.perform(get("/admin/servers"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminServerView"))
                .andExpect(model().attributeExists("servers"))
                .andExpect(model().attribute("servers", hasItem(hasProperty("serverName", is("Test Server")))))
                .andExpect(model().attribute("totalPages", is(1)))
                .andExpect(model().attribute("currentPage", is(0)))
                .andExpect(model().attribute("sortField", is("id")))
                .andExpect(model().attribute("sortDirection", is("asc")))
                .andExpect(model().attribute("reverseSortDirection", is("desc")));
    }

    @Test
    @DisplayName("GET /admin/servers/{id} - displays server detail view")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testAdminServerDetail() throws Exception {
        Server server = mockServer();
        when(serverFacade.findById(1L)).thenReturn(server);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(get("/admin/servers/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminServerDetail"))
                .andExpect(model().attributeExists("server"))
                .andExpect(model().attribute("server", hasProperty("id", is(1L))))
                .andExpect(model().attributeExists("comment"))
                .andExpect(model().attribute("currentUserEmail", is("admin@example.com")));
    }

    @Test
    @DisplayName("GET /admin/servers/{id} - redirects when server not found")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testAdminServerDetailNotFound() throws Exception {
        when(serverFacade.findById(999L)).thenReturn(null);

        mockMvc.perform(get("/admin/servers/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    @DisplayName("GET /admin/servers/edit/{id} - authorized admin access")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testShowEditServerAuthorized() throws Exception {
        Server server = mockServer();
        when(serverFacade.findById(1L)).thenReturn(server);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(get("/admin/servers/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminEditServer"))
                .andExpect(model().attribute("server", hasProperty("id", is(1L))));
    }

    @Test
    @DisplayName("GET /admin/servers/edit/{id} - unauthorized user")
    @WithMockUser(username = "notowner@example.com", roles = {"USER"})
    void testShowEditServerUnauthorized() throws Exception {
        Server server = mockServer();
        User differentUser = mockUser();
        differentUser.setEmail("notowner@example.com");
        differentUser.setRole("USER");

        when(serverFacade.findById(1L)).thenReturn(server);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(differentUser));

        mockMvc.perform(get("/admin/servers/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servers/1"));
    }

    @Test
    @DisplayName("POST /admin/servers/edit/{id} - valid edit by admin")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testEditServerValid() throws Exception {
        Server existingServer = mockServer();
        Server updatedServer = mockServer();
        updatedServer.setServerName("Updated Server");

        when(serverFacade.findById(1L)).thenReturn(existingServer);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(post("/admin/servers/edit/1")
                        .flashAttr("server", updatedServer))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servers/1"));

        verify(serverFacade).updateServer(existingServer, updatedServer);
    }

    @Test
    @DisplayName("POST /admin/servers/edit/{id} - with validation errors")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testEditServerWithValidationErrors() throws Exception {
        Server existingServer = mockServer();
        Server updatedServer = mockServer();
        updatedServer.setServerName(""); // Invalid: empty name

        when(serverFacade.findById(1L)).thenReturn(existingServer);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(post("/admin/servers/edit/1")
                        .flashAttr("server", updatedServer)
                        .param("serverName", "")) // simulate validation error
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminEditServer"));
    }

    @Test
    @DisplayName("GET /admin/servers/delete/{id} - delete server by admin")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testDeleteServerAsAdmin() throws Exception {
        Server server = mockServer();
        when(serverFacade.findById(1L)).thenReturn(server);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(get("/admin/servers/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servers"));

        verify(serverFacade).deleteServer(1L);
    }

    @Test
    @DisplayName("GET /admin/servers/delete/{id} - unauthorized user")
    @WithMockUser(username = "notowner@example.com", roles = {"USER"})
    void testDeleteServerUnauthorized() throws Exception {
        Server server = mockServer();
        User notOwner = mockUser();
        notOwner.setEmail("notowner@example.com");
        notOwner.setRole("USER");

        when(serverFacade.findById(1L)).thenReturn(server);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(notOwner));

        mockMvc.perform(get("/admin/servers/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servers"));

        verify(serverFacade, never()).deleteServer(1L);
    }

    @Test
    @DisplayName("POST /admin/servers/{id}/comments/{commentId}/delete - delete by comment owner")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testDeleteCommentByOwner() throws Exception {
        Comment comment = new Comment();
        comment.setId(5L);
        comment.setCreatedBy(mockUser());

        when(commentFacade.findById(5L)).thenReturn(Optional.of(comment));
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(post("/admin/servers/1/comments/5/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servers/1"));

        verify(commentFacade).deleteComment(5L);
    }

    @Test
    @DisplayName("POST /admin/servers/{id}/comments/{commentId}/delete - unauthorized user")
    @WithMockUser(username = "notowner@example.com", roles = {"USER"})
    void testDeleteCommentUnauthorized() throws Exception {
        Comment comment = new Comment();
        comment.setId(5L);
        User owner = mockUser();
        owner.setEmail("owner@example.com");
        comment.setCreatedBy(owner);

        User notOwner = mockUser();
        notOwner.setEmail("notowner@example.com");
        notOwner.setRole("USER");

        when(commentFacade.findById(5L)).thenReturn(Optional.of(comment));
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(notOwner));

        mockMvc.perform(post("/admin/servers/1/comments/5/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/servers/1"));

        verify(commentFacade, never()).deleteComment(5L);
    }

    // ====== TESTY DLA viewUser() ======

    @Test
    @DisplayName("viewUser - should return user list page successfully")
    @WithMockUser(roles = "ADMIN")
    void viewUser_Success() throws Exception {
        Page<User> userPage = new PageImpl<>(List.of(mockUser()));
        when(userFacade.findFilteredUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(userPage);

        mockMvc.perform(get("/admin/users")
                        .param("page", "0")
                        .param("sortField", "id")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminUserView"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("currentPage"));
    }

    @Test
    @DisplayName("viewUser - should redirect to dashboard with error if registration end date is before start date")
    @WithMockUser(roles = "ADMIN")
    void viewUser_InvalidRegistrationDates() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("startDateRegistration", "2025-06-10")
                        .param("endDateRegistration", "2025-06-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("viewUser - should redirect to dashboard with error if last login end date is before start date")
    @WithMockUser(roles = "ADMIN")
    void viewUser_InvalidLastLoginDates() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("startDateLastLogin", "2025-06-10")
                        .param("endDateLastLogin", "2025-06-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("error"));
    }


    // ====== TESTY DLA viewUserServers() ======

    @Test
    @DisplayName("viewUserServers - should redirect to login if user not authenticated")
    @WithMockUser(roles = "ADMIN")
    void viewUserServers_UnauthenticatedUser_RedirectToLogin() throws Exception {
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/users/1/servers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("viewUserServers - should redirect to users page with error if user not found")
    @WithMockUser(roles = "ADMIN")
    void viewUserServers_UserNotFound() throws Exception {
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));
        when(userFacade.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/admin/users/1/servers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("viewUserServers - should redirect with error if serverName contains invalid characters")
    @WithMockUser(roles = "ADMIN")
    void viewUserServers_InvalidServerName() throws Exception {
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));
        when(userFacade.findById(1L)).thenReturn(mockUser());

        mockMvc.perform(get("/admin/users/1/servers")
                        .param("serverName", "Invalid@Name!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("viewUserServers - should redirect with error if ipAddress is invalid")
    @WithMockUser(roles = "ADMIN")
    void viewUserServers_InvalidIpAddress() throws Exception {
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));
        when(userFacade.findById(1L)).thenReturn(mockUser());

        mockMvc.perform(get("/admin/users/1/servers")
                        .param("ipAddress", "Invalid#IP"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("viewUserServers - should redirect with error if minScore or maxScore is negative")
    @WithMockUser(roles = "ADMIN")
    void viewUserServers_NegativeScores() throws Exception {
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));
        when(userFacade.findById(1L)).thenReturn(mockUser());

        mockMvc.perform(get("/admin/users/1/servers")
                        .param("minScore", "-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        mockMvc.perform(get("/admin/users/1/servers")
                        .param("maxScore", "-5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("viewUserServers - should redirect with error if minScore is greater than maxScore")
    @WithMockUser(roles = "ADMIN")
    void viewUserServers_MinScoreGreaterThanMaxScore() throws Exception {
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));
        when(userFacade.findById(1L)).thenReturn(mockUser());

        mockMvc.perform(get("/admin/users/1/servers")
                        .param("minScore", "10")
                        .param("maxScore", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("viewUserServers - should redirect with error if created or updated dates are invalid or inconsistent")
    @WithMockUser(roles = "ADMIN")
    void viewUserServers_InvalidDates() throws Exception {
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));
        when(userFacade.findById(1L)).thenReturn(mockUser());

        // createdBefore < createdAfter
        mockMvc.perform(get("/admin/users/1/servers")
                        .param("startDate", "2025-06-10T00:00:00")
                        .param("endDate", "2025-06-01T00:00:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        // updatedBefore < updatedAfter
        mockMvc.perform(get("/admin/users/1/servers")
                        .param("updatedStartDate", "2025-06-10T00:00:00")
                        .param("updatedEndDate", "2025-06-01T00:00:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));

        // Invalid date format
        mockMvc.perform(get("/admin/users/1/servers")
                        .param("startDate", "not-a-date"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("viewUserServers - should return server list page successfully")
    @WithMockUser(roles = "ADMIN")
    void viewUserServers_Success() throws Exception {
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));
        when(userFacade.findById(1L)).thenReturn(mockUser());

        Page<Server> serverPage = new PageImpl<>(List.of(mockServer()));
        when(serverFacade.findFilteredServersForUser(
                anyLong(),
                nullable(String.class),
                nullable(String.class),
                nullable(ServerVersion.class),
                nullable(ServerMode.class),
                nullable(Integer.class),
                nullable(Integer.class),
                nullable(ServerStatus.class),
                nullable(LocalDateTime.class),
                nullable(LocalDateTime.class),
                nullable(LocalDateTime.class),
                nullable(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(serverPage);

        mockMvc.perform(get("/admin/users/1/servers")
                        .param("page", "0")
                        .param("sortField", "id")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminUserServersView"))
                .andExpect(model().attributeExists("servers"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("totalPages"));
    }


    // ====== TESTY DLA userServersDetail() ======

    @Test
    @DisplayName("userServersDetail - should redirect with error if server not found")
    @WithMockUser(roles = "ADMIN")
    void userServersDetail_ServerNotFound() throws Exception {
        when(serverFacade.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/admin/users/1/servers/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("userServersDetail - should redirect with error if user not found")
    @WithMockUser(roles = "ADMIN")
    void userServersDetail_UserNotFound() throws Exception {
        when(serverFacade.findById(1L)).thenReturn(mockServer());
        when(userFacade.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/admin/users/1/servers/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @DisplayName("userServersDetail - should return server detail page successfully")
    @WithMockUser(roles = "ADMIN")
    void userServersDetail_Success() throws Exception {
        when(serverFacade.findById(1L)).thenReturn(mockServer());
        when(userFacade.findById(1L)).thenReturn(mockUser());
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(get("/admin/users/1/servers/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminUserServersDetail"))
                .andExpect(model().attributeExists("server"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("comment"))
                .andExpect(model().attributeExists("currentUserEmail"));
    }

    @Test
    @DisplayName("GET /users/{userId}/servers/edit/{serverId} - redirect when user not found")
    void editUserServer_UserNotFound_redirects() throws Exception {
        when(userFacade.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/admin/users/1/servers/edit/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/1/servers"));
    }

    @Test
    @DisplayName("GET /users/{userId}/servers/edit/{serverId} - success view")
    void editUserServer_Success() throws Exception {
        User user = mockUser();
        Server server = mockServer();
        when(userFacade.findById(1L)).thenReturn(user);
        when(serverFacade.findById(2L)).thenReturn(server);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/admin/users/1/servers/edit/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminEditUserServer"))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("server", server));
    }

    @Test
    @DisplayName("POST /users/{userId}/servers/edit/{serverId} - form error returns edit page")
    void updateUserServer_WithErrors_ReturnsForm() throws Exception {
        Server server = mockServer();
        when(serverFacade.findById(2L)).thenReturn(server);
        when(userFacade.findById(1L)).thenReturn(mockUser());
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(post("/admin/users/1/servers/edit/2")
                        .param("name", "")  // puste pole powoduje error walidacji (przykład)
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminEditUserServer"))
                .andExpect(model().attributeExists("server"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("POST /users/{userId}/servers/edit/{serverId} - success redirects to server page")
    void updateUserServer_SuccessRedirect() throws Exception {
        Server existing = mockServer();
        when(serverFacade.findById(2L)).thenReturn(existing);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));
        when(userFacade.findById(1L)).thenReturn(mockUser());

        mockMvc.perform(post("/admin/users/1/servers/edit/2")
                        .param("serverName", "Test Server")
                        .param("ipAddress", "127.0.0.1")
                        .param("version", "V1_19_0")
                        .param("mode", "SURVIVAL")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/1/servers/2"));

        verify(serverFacade).updateServer(eq(existing), any(Server.class));
    }

    @Test
    @DisplayName("GET /users/{userId}/servers/delete/{serverId} - redirect with error if server not found or not authenticated")
    void deleteUserServer_NotFoundOrNoAuth_RedirectWithError() throws Exception {
        when(serverFacade.findById(2L)).thenReturn(null);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/users/1/servers/delete/2")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"))
                .andExpect(redirectedUrl("/admin/users/1/servers"));
    }

    @Test
    @DisplayName("GET /users/{userId}/servers/delete/{serverId} - success delete by owner")
    void deleteUserServer_SuccessAsOwner() throws Exception {
        User currentUser = mockUser();
        Server server = mockServer();
        server.setCreatedBy(currentUser);

        when(serverFacade.findById(2L)).thenReturn(server);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(currentUser));
        currentUser.setRole("USER");

        mockMvc.perform(get("/admin/users/1/servers/delete/2")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("success"))
                .andExpect(redirectedUrl("/admin/users/1/servers"));

        verify(serverFacade).deleteServer(2L);
    }

    @Test
    @DisplayName("GET /users/{userId}/servers/delete/{serverId} - no permission to delete returns error")
    void deleteUserServer_NoPermissionRedirectWithError() throws Exception {
        User currentUser = mockUser();
        currentUser.setRole("USER");
        User owner = mockUser();
        owner.setEmail("owner@example.com");

        Server server = mockServer();
        server.setCreatedBy(owner);

        when(serverFacade.findById(2L)).thenReturn(server);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(currentUser));

        mockMvc.perform(get("/admin/users/1/servers/delete/2")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"))
                .andExpect(redirectedUrl("/admin/users/1/servers/2"));

        verify(serverFacade, never()).deleteServer(anyLong());
    }

    @Test
    @DisplayName("GET /users/{id} - user detail not found shows dashboard with error")
    void showUserDetail_UserNotFound() throws Exception {
        when(userFacade.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @DisplayName("GET /users/{id} - user detail success")
    void showUserDetail_Success() throws Exception {
        User user = mockUser();
        when(userFacade.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminUserDetail"))
                .andExpect(model().attribute("user", user));
    }

    @Test
    @DisplayName("GET /users/advanced/{id} - redirect when accessing own advanced settings")
    void advancedUser_AccessDeniedForSelf() throws Exception {
        User user = mockUser();
        when(userFacade.findById(1L)).thenReturn(user);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/admin/users/advanced/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("error"))
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    @DisplayName("GET /users/advanced/{id} - success")
    void advancedUser_Success() throws Exception {
        User user = mockUser();
        User currentUser = mockUser();
        currentUser.setEmail("other@example.com");

        when(userFacade.findById(1L)).thenReturn(user);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(currentUser));

        mockMvc.perform(get("/admin/users/advanced/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/adminUserAdvanced"))
                .andExpect(model().attribute("user", user));
    }

    @Test
    @DisplayName("POST /users/advanced/{id} - redirect if user not found or self")
    void updatedAdvancedUser_RedirectsIfNotFoundOrSelf() throws Exception {
        when(userFacade.findById(1L)).thenReturn(null);

        mockMvc.perform(post("/admin/users/advanced/1")
                        .param("role", "ADMIN")
                        .param("ban", "on")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    @DisplayName("POST /users/advanced/{id} - success updates user role and ban")
    void updatedAdvancedUser_Success() throws Exception {
        User user = mockUser();
        User currentUser = mockUser();
        currentUser.setEmail("other@example.com");

        when(userFacade.findById(1L)).thenReturn(user);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(currentUser));

        mockMvc.perform(post("/admin/users/advanced/1")
                        .param("role", "ADMIN")
                        .param("ban", "on")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userFacade).update(argThat(u -> u.getRole().equals("ADMIN") && u.isBanned()));
    }

    @Test
    @DisplayName("POST /users/delete/{id} - success soft delete user")
    void deleteUser_Success() throws Exception {
        User user = mockUser();
        User currentUser = mockUser();
        currentUser.setEmail("other@example.com");

        when(userFacade.findById(1L)).thenReturn(user);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(currentUser));

        mockMvc.perform(post("/admin/users/delete/1")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userFacade).softDelete(user);
    }

    @Test
    @DisplayName("POST /users/delete/{id} - no soft delete when deleting self")
    void deleteUser_Self_NoDeletion() throws Exception {
        User user = mockUser();

        when(userFacade.findById(1L)).thenReturn(user);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(user));

        mockMvc.perform(post("/admin/users/delete/1")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userFacade, never()).softDelete(any());
    }

    @Test
    @DisplayName("POST /user/{userId}/servers/{serverId}/comments/{commentId}/delete - delete comment if permitted")
    void deleteComment_Success() throws Exception {
        Comment comment = mockComment();
        User user = mockUser();
        when(commentFacade.findById(3L)).thenReturn(Optional.of(comment));
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(user));

        comment.getCreatedBy().setId(1L);
        comment.getCreatedBy().setEmail("test@example.com");
        user.setEmail("test@example.com");
        user.setRole("USER");

        mockMvc.perform(post("/admin/user/1/servers/2/comments/3/delete")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection());

        verify(commentFacade).deleteComment(3L);
    }
}
