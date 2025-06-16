package com.example.elitemcservers.repository;

import com.example.elitemcservers.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@TestPropertySource(properties = "spring.datasource.initialization-mode=never")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setProfileImage("/img/default_profile.png");
        user.setRole("USER");
        user.setBanned(false);
        user.setDeleted(false);
        user.setRegistrationDate(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        return user;
    }

    @Test
    @DisplayName("Should save user and find by ID")
    public void testSaveAndFindId() {
        User user = createTestUser("testUser", "testUser@example.com");

        User savedUser = userRepository.save(user);
        entityManager.flush();

        assertThat(savedUser.getId()).isNotNull();

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("Should find user by email")
    public void testFindByEmail() {
        User user = createTestUser("emailUser", "emailUser@example.com");

        entityManager.persistAndFlush(user);

        Optional<User> foundUser = userRepository.findByEmail("emailUser@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("emailUser");
    }

    @Test
    @DisplayName("Should find user by username")
    public void testFindByUsername() {
        User user = createTestUser("uniqueUsername", "unique@example.com");

        entityManager.persistAndFlush(user);

        Optional<User> foundUser = userRepository.findByUsername("uniqueUsername");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("unique@example.com");
    }

    @Test
    @DisplayName("Should update user data")
    public void testUpdateUser() {
        User user = createTestUser("updateUser", "updateUser@example.com");

        User savedUser = entityManager.persistAndFlush(user);

        savedUser.setUsername("updatedUser");
        savedUser.setEmail("updatedEmail@example.com");
        savedUser.setProfileImage("/img/updated_profile.png");

        User updatedUser = userRepository.save(savedUser);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findById(updatedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("updatedUser");
        assertThat(foundUser.get().getEmail()).isEqualTo("updatedEmail@example.com");
        assertThat(foundUser.get().getProfileImage()).isEqualTo("/img/updated_profile.png");
    }

    @Test
    @DisplayName("Should delete user")
    public void testDeleteUser() {
        User user = createTestUser("deleteUser", "deleteUser@example.com");

        User savedUser = entityManager.persistAndFlush(user);

        userRepository.delete(savedUser);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isNotPresent();
    }

    @Test
    @DisplayName("Should enforce unique constraints on username and email")
    public void testUniqueConstraints() {
        User user1 = createTestUser("uniqueUser", "uniqueUser@example.com");
        entityManager.persistAndFlush(user1);

        User user2 = createTestUser("uniqueUser", "another@example.com");
        assertThrows(Exception.class, () -> userRepository.saveAndFlush(user2));

        User user3 = createTestUser("anotherUser", "uniqueUser@example.com");
        assertThrows(Exception.class, () -> userRepository.saveAndFlush(user3));
    }
}

