package com.ddf.vodsystem.security;

import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.repositories.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauthUser = super.loadUser(userRequest);

        String googleId = oauthUser.getAttribute("sub"); // Google's unique user ID
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        Optional<User> userOptional = userRepository.findByGoogleId(googleId);
        User user;
        if (userOptional.isEmpty()) {
            user = new User();
            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setName(name);
            user.setUsername(email.split("@")[0]);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        return oauthUser;
    }
}