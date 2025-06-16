package com.example.elitemcservers.service;

import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerStatus;
import com.example.elitemcservers.enums.ServerVersion;
import com.example.elitemcservers.repository.ServerRepository;
import com.example.elitemcservers.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ServerServiceTest {

    @Autowired
    private ServerService serverService;

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private UserRepository userRepository;

    private User createValidUser() {
        User user = new User();
        user.setUsername("serveruser");
        user.setEmail("serveruser" + System.currentTimeMillis() + "@example.com");
        user.setPassword("securePass123");
        user.setRole("USER");
        user.setBanned(false);
        user.setDeleted(false);
        user.setRegistrationDate(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Server createValidServer(User user) {
        Server server = new Server();
        server.setServerName("TestServer");
        server.setIpAddress("192.168.1.1");
        server.setCreatedBy(user);
        server.setStatus(ServerStatus.PENDING);
        server.setVersion(ServerVersion.V1_18_0);
        server.setMode(ServerMode.SURVIVAL);
        server.setUpVotes(0);
        server.setDownVotes(0);
        server.setScore(0);
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());
        return serverRepository.save(server);
    }

    @Test
    @DisplayName("Should create and save Server")
    public void testCreateServer() {
        User user = createValidUser();

        Server server = new Server();
        server.setServerName("MyNewServer");
        server.setIpAddress("123.123.123.123");
        server.setVersion(ServerVersion.V1_19_0);
        server.setMode(ServerMode.ADVENTURE);

        Server created = serverService.createServer(server, user);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreatedBy()).isEqualTo(user);
        assertThat(created.getStatus()).isEqualTo(ServerStatus.PENDING);
    }

    @Test
    @DisplayName("Should find Server by ID")
    public void testFindById() {
        User user = createValidUser();
        Server server = createValidServer(user);

        Server found = serverService.findById(server.getId());

        assertThat(found).isNotNull();
        assertThat(found.getServerName()).isEqualTo(server.getServerName());
    }

    @Test
    @DisplayName("Should update Server")
    public void testUpdateServer() {
        User user = createValidUser();
        Server server = createValidServer(user);

        server.setServerName("UpdatedServerName");
        Server updated = serverService.updateServer(server);

        assertThat(updated.getServerName()).isEqualTo("UpdatedServerName");

        Optional<Server> fetched = serverRepository.findById(server.getId());
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getServerName()).isEqualTo("UpdatedServerName");
    }

    @Test
    @DisplayName("Should delete Server")
    public void testDeleteServer() {
        User user = createValidUser();
        Server server = createValidServer(user);

        serverService.deleteServer(server.getId());

        Optional<Server> deleted = serverRepository.findById(server.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should upVote and update score")
    public void testUpVote() {
        User user = createValidUser();
        Server server = createValidServer(user);

        server.setUpVotes(5);
        server.setDownVotes(2);
        server.setScore(3);
        serverRepository.save(server);

        serverService.upVote(server);

        Optional<Server> updated = serverRepository.findById(server.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getUpVotes()).isEqualTo(6);
        assertThat(updated.get().getScore()).isEqualTo(4); // 6 - 2
    }

    @Test
    @DisplayName("Should downVote and update score")
    public void testDownVote() {
        User user = createValidUser();
        Server server = createValidServer(user);

        server.setUpVotes(5);
        server.setDownVotes(2);
        server.setScore(3);
        serverRepository.save(server);

        serverService.downVote(server);

        Optional<Server> updated = serverRepository.findById(server.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getDownVotes()).isEqualTo(3);
        assertThat(updated.get().getScore()).isEqualTo(2); // 5 - 3
    }

    @Test
    @DisplayName("Should find Server by serverName")
    public void testFindByServerName() {
        User user = createValidUser();
        Server server = createValidServer(user);

        Optional<Server> found = serverService.findByServerName(server.getServerName());

        assertThat(found).isPresent();
        assertThat(found.get().getServerName()).isEqualTo(server.getServerName());
    }

    @Test
    @DisplayName("Should filter Servers")
    public void testFindFilteredServers() {
        User user = createValidUser();

        Server server1 = createValidServer(user);
        server1.setServerName("AlphaServer");
        server1.setScore(10);
        server1.setStatus(ServerStatus.APPROVED);
        serverRepository.save(server1);

        Server server2 = createValidServer(user);
        server2.setServerName("BetaServer");
        server2.setScore(5);
        server2.setStatus(ServerStatus.APPROVED);
        serverRepository.save(server2);

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Server> filtered = serverService.findFilteredServers(
                null,
                "AlphaServer",
                null,
                null,
                null,
                5,
                15,
                pageable
        );

        assertThat(filtered.getContent()).hasSize(1);
        assertThat(filtered.getContent().get(0).getServerName()).isEqualTo("AlphaServer");
    }

}
