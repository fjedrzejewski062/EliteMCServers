package com.example.elitemcservers.service;

import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should register a new user")
    void testRegisterUser() {
        User user = new User();
        user.setUsername("newUser");
        user.setEmail("newUser@example.com");
        user.setPassword("plainPassword");

        User registered = userService.register(user);

        assertThat(registered.getId()).isNotNull();
        assertThat(registered.getPassword()).isNotEqualTo("plainPassword");
        assertThat(passwordEncoder.matches("plainPassword", registered.getPassword())).isTrue();
        assertThat(registered.getRegistrationDate()).isNotNull();
        assertThat(registered.isBanned()).isFalse();
        assertThat(registered.isDeleted()).isFalse();

        Optional<User> found = userRepository.findByEmail("newUser@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("newUser");
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        User user = new User();
        user.setUsername("emailUser");
        user.setEmail("emailUser@example.com");
        user.setPassword("password123");

        userService.register(user);

        Optional<User> found = userService.findByEmail("emailUser@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("emailUser");
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername() {
        User user = new User();
        user.setUsername("findMe");
        user.setEmail("findme@example.com");
        user.setPassword("password123");

        userService.register(user);

        Optional<User> found = userService.findByUsername("findMe");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("findme@example.com");
    }

    @Test
    @DisplayName("Should update user data")
    void testUpdateUser() {
        User user = new User();
        user.setUsername("updateUser");
        user.setEmail("updateUser@example.com");
        user.setPassword("password123");

        User saved = userService.register(user);
        saved.setUsername("updatedUsername");
        saved.setEmail("updated@example.com");

        User updated = userService.update(saved);

        assertThat(updated.getUsername()).isEqualTo("updatedUsername");
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("Should soft delete user")
    void testSoftDeleteUser() {
        User user = new User();
        user.setUsername("softDelete");
        user.setEmail("softDelete@example.com");
        user.setPassword("password123");

        User saved = userService.register(user);
        userService.softDelete(saved);

        User fetched = userService.findById(saved.getId());
        assertThat(fetched).isNotNull();
        assertThat(fetched.isDeleted()).isTrue();
        assertThat(fetched.getUsername()).startsWith("DELETED-USER-");
        assertThat(fetched.getEmail()).startsWith("DELETED-USER-");
        assertThat(passwordEncoder.matches("DELETED-PASSWORD", fetched.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Should find user by ID")
    void testFindById() {
        User user = new User();
        user.setUsername("idUser");
        user.setEmail("id@example.com");
        user.setPassword("password123");

        User saved = userService.register(user);
        User fetched = userService.findById(saved.getId());

        assertThat(fetched).isNotNull();
        assertThat(fetched.getUsername()).isEqualTo("idUser");
    }

    @Test
    @DisplayName("Should filter users by multiple criteria")
    void testFindFilteredUsers() {
        User user1 = new User();
        user1.setUsername("filterUser1");
        user1.setEmail("filter1@example.com");
        user1.setPassword("password");
        user1.setRole("USER");
        user1.setBanned(false);
        user1.setDeleted(false);
        user1.setRegistrationDate(LocalDateTime.of(2024, 1, 1, 10, 0));
        user1.setLastLogin(LocalDateTime.of(2024, 2, 1, 12, 0));
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("filterUser2");
        user2.setEmail("filter2@example.com");
        user2.setPassword("password");
        user2.setRole("ADMIN");
        user2.setBanned(true);
        user2.setDeleted(false);
        user2.setRegistrationDate(LocalDateTime.of(2024, 3, 10, 14, 0));
        user2.setLastLogin(LocalDateTime.of(2024, 3, 11, 9, 0));
        userRepository.save(user2);

        Pageable pageable = Pageable.ofSize(10);
        Page<User> result = userService.findFilteredUsers(
                "filterUser",
                "filter2",
                "ADMIN",
                true,
                false,
                "2024-03-01",
                "2024-03-31",
                "2024-03-01",
                "2024-03-31",
                pageable
        );

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("filterUser2");
    }
}
