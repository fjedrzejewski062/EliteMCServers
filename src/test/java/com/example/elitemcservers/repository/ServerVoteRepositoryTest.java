package com.example.elitemcservers.repository;

import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.ServerVote;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerStatus;
import com.example.elitemcservers.enums.ServerVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = "spring.datasource.initialization-mode=never")
class ServerVoteRepositoryTest {

    @Autowired
    private ServerVoteRepository serverVoteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerRepository serverRepository;

    private User createValidUser(String suffix) {
        User user = new User();
        user.setUsername("voter" + suffix);
        user.setEmail("voter" + suffix + System.currentTimeMillis() + "@example.com");
        user.setPassword("password123");
        user.setProfileImage("/img/default.png");
        user.setRole("USER");
        user.setBanned(false);
        user.setDeleted(false);
        user.setRegistrationDate(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Server createValidServer(User user, String suffix) {
        Server server = new Server();
        server.setServerName("VoteServer" + suffix);
        server.setIpAddress("127.0.0." + suffix);
        server.setVersion(ServerVersion.V1_20_4);
        server.setMode(ServerMode.SURVIVAL);
        server.setDescription("Test server for voting");
        server.setStatus(ServerStatus.APPROVED);
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());
        server.setCreatedBy(user);
        return serverRepository.save(server);
    }

    private ServerVote createVote(User user, Server server, String voteType) {
        ServerVote vote = new ServerVote();
        vote.setUser(user);
        vote.setServer(server);
        vote.setVoteType(voteType);
        return serverVoteRepository.save(vote);
    }

    @Test
    @DisplayName("Should save and find vote by user and server")
    void testFindByUserAndServer() {
        User user = createValidUser("A");
        Server server = createValidServer(user, "1");

        ServerVote vote = createVote(user, server, "UP");

        Optional<ServerVote> result = serverVoteRepository.findByUserAndServer(user, server);

        assertTrue(result.isPresent());
        assertEquals("UP", result.get().getVoteType());
    }

    @Test
    @DisplayName("Should enforce unique constraint on user and server")
    void testUniqueConstraint() {
        User user = createValidUser("B");
        Server server = createValidServer(user, "2");

        createVote(user, server, "DOWN");

        ServerVote duplicateVote = new ServerVote();
        duplicateVote.setUser(user);
        duplicateVote.setServer(server);
        duplicateVote.setVoteType("UP");

        assertThrows(Exception.class, () -> serverVoteRepository.saveAndFlush(duplicateVote));
    }
}
