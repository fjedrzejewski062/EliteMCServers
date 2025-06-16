package com.example.elitemcservers.controller;

import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerVersion;
import com.example.elitemcservers.facade.ServerFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.data.domain.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HomeControllerTest {

    private ServerFacade serverFacade;
    private HomeController homeController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        serverFacade = mock(ServerFacade.class);
        homeController = new HomeController(serverFacade);
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
    }

    @Test
    @DisplayName("GET / - returns home view with servers list")
    void testHomeSuccess() throws Exception {
        Server server = new Server();
        server.setServerName("Test Server");
        List<Server> serverList = List.of(server);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<Server> serverPage = new PageImpl<>(serverList, pageable, 1);

        when(serverFacade.findFilteredServers(
                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(serverPage);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("servers"))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("sortField", "id"))
                .andExpect(model().attribute("sortDirection", "asc"));

        verify(serverFacade).findFilteredServers(
                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
    }


    @Test
    @DisplayName("GET / with invalid serverName redirects with error")
    void testHomeInvalidServerName() throws Exception {
        mockMvc.perform(get("/").param("serverName", "Invalid@Name!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));

        verifyNoInteractions(serverFacade);
    }

    @Test
    @DisplayName("GET / with invalid ipAddress redirects with error")
    void testHomeInvalidIpAddress() throws Exception {
        mockMvc.perform(get("/").param("ipAddress", "Invalid#IP"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));

        verifyNoInteractions(serverFacade);
    }

    @Test
    @DisplayName("GET / with negative scores redirects with error")
    void testHomeNegativeScores() throws Exception {
        mockMvc.perform(get("/").param("minScore", "-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));

        mockMvc.perform(get("/").param("maxScore", "-5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));

        verifyNoInteractions(serverFacade);
    }

    @Test
    @DisplayName("GET / with minScore > maxScore redirects with error")
    void testHomeMinScoreGreaterThanMaxScore() throws Exception {
        mockMvc.perform(get("/")
                        .param("minScore", "10")
                        .param("maxScore", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));

        verifyNoInteractions(serverFacade);
    }

    @Test
    @DisplayName("GET / with invalid date range redirects with error")
    void testHomeInvalidDateRange() throws Exception {
        mockMvc.perform(get("/")
                        .param("startDate", "2025-06-10")
                        .param("endDate", "2025-06-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));

        verifyNoInteractions(serverFacade);
    }

    @Test
    @DisplayName("GET / with invalid date format redirects with error")
    void testHomeInvalidDateFormat() throws Exception {
        mockMvc.perform(get("/")
                        .param("startDate", "invalid-date")
                        .param("endDate", "2025-06-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("error"));

        verifyNoInteractions(serverFacade);
    }

    @Test
    @DisplayName("GET / with sorting and paging parameters works correctly")
    void testHomeWithSortingAndPaging() throws Exception {
        Server server = new Server();
        server.setServerName("Sorted Server");
        List<Server> serverList = List.of(server);
        Pageable pageable = PageRequest.of(1, 10, Sort.by("score").descending());
        Page<Server> serverPage = new PageImpl<>(serverList, pageable, 1);

        when(serverFacade.findFilteredServers(
                any(),
                nullable(String.class),
                nullable(String.class),
                any(),
                any(),
                any(),
                any(),
                any(Pageable.class)))
                .thenReturn(serverPage);

        mockMvc.perform(get("/")
                        .param("page", "1")
                        .param("sortField", "score")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("sortField", "score"))
                .andExpect(model().attribute("sortDirection", "desc"))
                .andExpect(model().attributeExists("servers"));

        verify(serverFacade).findFilteredServers(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class));
    }

}
