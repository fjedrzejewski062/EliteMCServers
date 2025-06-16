package com.example.elitemcservers.service;

import com.example.elitemcservers.entity.Comment;
import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerStatus;
import com.example.elitemcservers.enums.ServerVersion;
import com.example.elitemcservers.repository.CommentRepository;
import com.example.elitemcservers.repository.ServerRepository;
import com.example.elitemcservers.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;

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
        server.setServerName("TestServer" + System.currentTimeMillis());
        server.setCreatedBy(user);
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());  // <-- tutaj ustawiamy updatedAt, którego brakowało
        server.setMode(ServerMode.SURVIVAL);
        server.setStatus(ServerStatus.APPROVED);
        server.setScore(10);
        server.setVersion(ServerVersion.V1_18_0);      // <-- dopisz też wersję, bo jest not null
        return serverRepository.save(server);
    }

    private Comment createValidComment(User user, Server server) {
        Comment comment = new Comment();
        comment.setContent("Test comment");
        comment.setCreatedBy(user);
        comment.setServer(server);
        comment.setCreationDate(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Test
    @DisplayName("Should save and retrieve Comment")
    public void testSaveAndFindById() {
        User user = createValidUser();
        Server server = createValidServer(user);

        Comment comment = new Comment();
        comment.setContent("Hello World");
        comment.setCreatedBy(user);
        comment.setServer(server);
        comment.setCreationDate(LocalDateTime.now());

        Comment savedComment = commentService.save(comment);

        Optional<Comment> foundComment = commentService.findById(savedComment.getId());

        assertThat(foundComment).isPresent();
        assertThat(foundComment.get().getContent()).isEqualTo("Hello World");
        assertThat(foundComment.get().getServer().getId()).isEqualTo(server.getId());
        assertThat(foundComment.get().getCreatedBy().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Should delete Comment")
    public void testDeleteComment() {
        User user = createValidUser();
        Server server = createValidServer(user);
        Comment comment = createValidComment(user, server);

        commentService.deleteComment(comment.getId());

        Optional<Comment> deleted = commentRepository.findById(comment.getId());

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should find comments by Server ID")
    public void testFindCommentsByServerId() {
        User user = createValidUser();
        Server server = createValidServer(user);

        Comment comment1 = createValidComment(user, server);
        Comment comment2 = createValidComment(user, server);

        List<Comment> comments = commentService.findCommentsByServerId(server.getId());

        assertThat(comments).hasSize(2);
        assertThat(comments).extracting("id").containsExactlyInAnyOrder(comment1.getId(), comment2.getId());
    }
}
