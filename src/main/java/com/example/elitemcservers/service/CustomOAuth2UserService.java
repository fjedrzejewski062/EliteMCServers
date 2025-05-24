package com.example.elitemcservers.service;

import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.repository.UserRepository;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends OidcUserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String username = generateUsernameFromEmail(email);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setPassword(UUID.randomUUID().toString()); // placeholder
            newUser.setProfileImage(oidcUser.getPicture());
            newUser.setRegistrationDate(LocalDateTime.now());
            newUser.setLastLogin(LocalDateTime.now());
            newUser.setRole("USER");
            return userRepository.save(newUser);
        });

        // Ustaw lastLogin przy każdej autoryzacji
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return oidcUser; // or return new DefaultOidcUser(...), if needed for authorities
    }

    private String generateUsernameFromEmail(String email) {
        String raw = email.substring(0, email.indexOf("@"));
        // Usuń znaki niepasujące do walidacji
        return raw.replaceAll("[^a-zA-Z0-9_.-]", "");
    }
}
