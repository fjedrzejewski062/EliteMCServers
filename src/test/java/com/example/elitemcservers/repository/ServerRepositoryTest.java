package com.example.elitemcservers.repository;

import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerStatus;
import com.example.elitemcservers.enums.ServerVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = "spring.datasource.initialization-mode=never")
class ServerRepositoryTest {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private UserRepository userRepository;

    private User createValidUser() {
        User user = new User();
        user.setUsername("serveruser");
        user.setEmail("serveruser" + System.currentTimeMillis() + "@example.com");
        user.setPassword("securePass123");
        user.setProfileImage("/img/default_profile.png");
        user.setRole("USER");
        user.setBanned(false);
        user.setDeleted(false);
        user.setRegistrationDate(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Server createValidServer(String name, User user) {
        Server server = new Server();
        server.setServerName(name);
        server.setIpAddress("127.0.0.1");
        server.setVersion(ServerVersion.V1_20_4);
        server.setMode(ServerMode.SURVIVAL);
        server.setDescription("Test server description");
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());
        server.setStatus(ServerStatus.APPROVED);
        server.setCreatedBy(user);
        return server;
    }

    @Test
    @DisplayName("Should find server by serverName")
    void testFindByServerName() {
        User user = createValidUser();
        Server server = createValidServer("TestServer", user);
        serverRepository.save(server);

        Optional<Server> result = serverRepository.findByServerName("TestServer");

        assertTrue(result.isPresent());
        assertEquals("127.0.0.1", result.get().getIpAddress());
    }

    @Test
    @DisplayName("Should find servers by creator")
    void testFindByCreatedBy() {
        User user = createValidUser();

        Server server1 = createValidServer("Server1", user);
        Server server2 = createValidServer("Server2", user);

        serverRepository.saveAll(List.of(server1, server2));

        List<Server> result = serverRepository.findByCreatedBy(user);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return paginated servers for given creator")
    void testFindByCreatedByWithPaging() {
        User user = createValidUser();

        for (int i = 0; i < 12; i++) {
            Server server = createValidServer("PagedServer" + i, user);
            serverRepository.save(server);
        }

        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<Server> page = serverRepository.findByCreatedBy(user, pageable);

        assertEquals(5, page.getContent().size());
        assertEquals(3, page.getTotalPages());
        assertEquals(12, page.getTotalElements());
    }
}
