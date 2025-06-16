package com.example.elitemcservers.repository;

import com.example.elitemcservers.entity.Comment;
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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = "spring.datasource.initialization-mode=never")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerRepository serverRepository;

    private User createValidUser() {
        User user = new User();
        user.setUsername("commentuser");
        user.setEmail("commentuser" + System.currentTimeMillis() + "@example.com");
        user.setPassword("securePass123");
        user.setProfileImage("/img/default_profile.png");
        user.setRole("USER");
        user.setBanned(false);
        user.setDeleted(false);
        user.setRegistrationDate(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Server createValidServer(User user) {
        Server server = new Server();
        server.setServerName("CommentedServer");
        server.setIpAddress("192.168.1.1");
        server.setVersion(ServerVersion.V1_20_4);
        server.setMode(ServerMode.SURVIVAL);
        server.setDescription("Some server for comments");
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());
        server.setStatus(ServerStatus.APPROVED);
        server.setCreatedBy(user);
        return serverRepository.save(server);
    }

    private Comment createComment(User user, Server server, String content) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreationDate(LocalDateTime.now());
        comment.setCreatedBy(user);
        comment.setServer(server);
        return commentRepository.save(comment);
    }

    @Test
    @DisplayName("Should find comments by serverId")
    void testFindByServerId() {
        User user = createValidUser();
        Server server = createValidServer(user);

        createComment(user, server, "This is comment 1");
        createComment(user, server, "This is comment 2");

        List<Comment> comments = commentRepository.findByServerId(server.getId());

        assertEquals(2, comments.size());
    }

    @Test
    @DisplayName("Should find comments by creator")
    void testFindByCreatedBy() {
        User user = createValidUser();
        Server server = createValidServer(user);

        createComment(user, server, "First user comment");
        createComment(user, server, "Second user comment");

        List<Comment> comments = commentRepository.findByCreatedBy(user);

        assertEquals(2, comments.size());
    }

    @Test
    @DisplayName("Should return paged comments for given creator")
    void testFindByCreatedByWithPaging() {
        User user = createValidUser();
        Server server = createValidServer(user);

        for (int i = 0; i < 12; i++) {
            createComment(user, server, "Paged comment " + i);
        }

        Pageable pageable = PageRequest.of(0, 5, Sort.by("creationDate").descending());
        Page<Comment> page = commentRepository.findByCreatedBy(user, pageable);

        assertEquals(5, page.getContent().size());
        assertEquals(3, page.getTotalPages());
    }
}
