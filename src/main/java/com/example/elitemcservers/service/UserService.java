package com.example.elitemcservers.service;

import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public User register(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRegistrationDate(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        user.setBanned(false);
        user.setDeleted(false);
        return userRepository.save(user);
    }
    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }
    public Optional<User> findByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public User update(User user){
        return userRepository.save(user);
    }

    public void softDelete(User user){
        //userRepository.delete(user);
        user.setDeleted(true);
        user.setUsername("DELETED-USER-" + user.getId());
        user.setEmail("DELETED-USER-" + user.getId() + "@elitemcservers.com");
        user.setPassword(passwordEncoder.encode("DELETED-PASSWORD"));
        userRepository.save(user);
    }

    public List<User> findAllUsers(){
        return userRepository.findAll();
    }

    public User findById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    public Page<User> findFilteredUsers(String username, String email, String role, Boolean banned, Boolean deleted,
                                        String startDateRegistration, String endDateRegistration, String startDateLastLogin, String endDateLastLogin, Pageable pageable) {

        Specification<User> spec = Specification.where(null);

        if (username != null && !username.isEmpty()) {
            spec = spec.and((root, query, builder) -> builder.like(builder.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }

        if (email != null && !email.isEmpty()) {
            spec = spec.and((root, query, builder) -> builder.like(builder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }

        if (role != null && !role.isEmpty()) {
            spec = spec.and((root, query, builder) -> builder.like(builder.lower(root.get("role")), "%" + role.toLowerCase() + "%"));
        }

        if (banned != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("banned"), banned));
        }

        if (deleted != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("deleted"), deleted));
        }

        if (startDateRegistration != null && !startDateRegistration.isEmpty() && endDateRegistration != null && !endDateRegistration.isEmpty()) {
            LocalDateTime startRegistration = LocalDateTime.parse(startDateRegistration + "T00:00:00");
            LocalDateTime endRegistration = LocalDateTime.parse(endDateRegistration + "T23:59:59");
            spec = spec.and((root, query, builder) -> builder.between(root.get("registrationDate"), startRegistration, endRegistration));
        }

        if (startDateLastLogin != null && !startDateLastLogin.isEmpty() && endDateLastLogin != null && !endDateLastLogin.isEmpty()) {
            LocalDateTime startLastLogin = LocalDateTime.parse(startDateLastLogin + "T00:00:00");
            LocalDateTime endLastLogin = LocalDateTime.parse(endDateLastLogin + "T23:59:59");
            spec = spec.and((root, query, builder) -> builder.between(root.get("lastLogin"), startLastLogin, endLastLogin));
        }

        return userRepository.findAll(spec, pageable);
    }

    public Optional<User> getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String email = null;

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            Object principal = oauthToken.getPrincipal();
            if (principal instanceof OidcUser oidcUser) {
                email = oidcUser.getEmail();
            } else {
                email = oauthToken.getPrincipal().getAttribute("email");
            }
        } else {
            email = authentication.getName();
        }

        if (email == null) return Optional.empty();
        return findByEmail(email);
    }
}
