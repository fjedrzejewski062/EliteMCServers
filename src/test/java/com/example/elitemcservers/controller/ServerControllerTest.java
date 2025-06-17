package com.example.elitemcservers.controller;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ServerControllerTest {

    private MockMvc mockMvc;

    private ServerFacade serverFacade;
    private UserFacade userFacade;
    private CommentFacade commentFacade;

    private ServerController serverController;

    @BeforeEach
    void setup() {
        serverFacade = Mockito.mock(ServerFacade.class);
        userFacade = Mockito.mock(UserFacade.class);
        commentFacade = Mockito.mock(CommentFacade.class);

        serverController = new ServerController(serverFacade, userFacade, commentFacade);

        // standaloneSetup bez Spring Boot Context
        mockMvc = MockMvcBuilders.standaloneSetup(serverController).build();
    }

    private User mockUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setProfileImage("/img/default_profile.png");
        user.setRole("USER");
        user.setBanned(false);
        user.setDeleted(false);
        user.setRegistrationDate(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        return user;
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
        server.setUpVotes(0);
        server.setDownVotes(0);
        server.setScore(0);
        server.setStatus(ServerStatus.PENDING);
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());
        return server;
    }

    @Test
    @DisplayName("GET /servers/create - displays server creation form")
    public void testShowCreateServerForm() throws Exception {
        mockMvc.perform(get("/servers/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("createServer"))
                .andExpect(model().attributeExists("server"));
    }

    @Test
    @DisplayName("POST /servers/create - creates server when valid data")
    @WithMockUser(username = "test@example.com")
    public void testCreateServerSuccess() throws Exception {
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(post("/servers/create")
                        .param("name", "New Server")
                        .param("description", "Some description"))
                .andExpect(status().isOk())
                .andExpect(view().name("createServer_success"));

        verify(serverFacade).createServer(any(Server.class), any(User.class));
    }

    @Test
    @DisplayName("POST /servers/create - creates server when valid data")
    @WithMockUser(username = "test@example.com")
    public void testCreateServerValidationFail() throws Exception {
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(post("/servers/create")
                        .param("name", "New Server")
                        .param("description", "Some description"))
                .andExpect(status().isOk())
                .andExpect(view().name("createServer_success"));

        verify(serverFacade).createServer(any(Server.class), any(User.class));
    }

    @Test
    @DisplayName("GET /servers/{id} - displays server details")
    public void testServerDetail() throws Exception {
        Server server = mockServer();
        when(serverFacade.findById(1L)).thenReturn(server);

        mockMvc.perform(get("/servers/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("serverDetail"))
                .andExpect(model().attribute("server", hasProperty("id", is(1L))))
                .andExpect(model().attributeExists("comment"));
    }

    @Test
    @DisplayName("POST /servers/{id}/vote - user can vote for server")
    @WithMockUser(username = "test@example.com")
    public void testVoteServer() throws Exception {
        Server server = mockServer();
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));
        when(serverFacade.findById(1L)).thenReturn(server);

        mockMvc.perform(post("/servers/1/vote").param("vote", "up"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(serverFacade).voteServer(eq(server), eq("up"), any(User.class));
    }

    @Test
    @DisplayName("POST /servers/{id}/comment - adds comment")
    @WithMockUser(username = "test@example.com")
    public void testAddComment() throws Exception {
        Server server = mockServer();
        when(serverFacade.findById(1L)).thenReturn(server);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(post("/servers/1/comment")
                        .param("content", "Great server!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/servers/1"));

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(serverFacade).addComment(eq(server), commentCaptor.capture(), any(User.class));
        verify(commentFacade).save(any(Comment.class));
    }


    @Test
    @DisplayName("GET /servers/edit/{id} - shows edit form for user's server")
    @WithMockUser(username = "test@example.com")
    public void testShowEditServerForm() throws Exception {
        Server server = mockServer();
        when(serverFacade.findById(1L)).thenReturn(server);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(get("/servers/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("editServer"))
                .andExpect(model().attribute("server", server));
    }

    @Test
    @DisplayName("POST /servers/edit/{id} - edits server successfully")
    @WithMockUser(username = "test@example.com")
    public void testEditServerSuccess() throws Exception {
        Server existingServer = mockServer();
        when(serverFacade.findById(1L)).thenReturn(existingServer);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(post("/servers/edit/1")
                        .param("name", "Updated Server")
                        .param("description", "Updated description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/servers/1"));

        verify(serverFacade).updateServer(any(Server.class), any(Server.class));
    }

    @Test
    @DisplayName("GET /servers/delete/{id} - deletes user's server")
    @WithMockUser(username = "test@example.com")
    public void testDeleteServer() throws Exception {
        Server server = mockServer();
        when(serverFacade.findById(1L)).thenReturn(server);
        when(userFacade.getAuthenticatedUser(any())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(get("/servers/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(serverFacade).deleteServer(1L);
    }
}
