package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.repositories.UserRepository;
import com.ddf.vodsystem.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
public class UserService {
    private final GoogleIdTokenVerifier verifier;
    private final UserRepository userRepository;
    private final JwtService jwtService;


    public UserService(UserRepository userRepository,
                       JwtService jwtService,
                       @Value("${google.client.id}") String googleClientId) {
        this.userRepository = userRepository;

        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();

        this.verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        this.jwtService = jwtService;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotAuthenticated("User not found"));
    }

    public User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    public String login(String idToken) {
        GoogleIdToken googleIdToken = getGoogleIdToken(idToken);
        String googleId = googleIdToken.getPayload().getSubject();

        if (googleId == null) {
            throw new NotAuthenticated("Invalid ID token");
        }

        Optional<User> existingUser = userRepository.findByGoogleId(googleId);

        if (existingUser.isEmpty()) {
            User user = createUserFromIdToken(googleIdToken);
            userRepository.saveAndFlush(user);
            return jwtService.generateToken(user.getId());
        }

        return jwtService.generateToken(existingUser.get().getId());
    }

    private User createUserFromIdToken(GoogleIdToken idToken) {
        String googleId = idToken.getPayload().getSubject();
        String email = idToken.getPayload().getEmail();
        String name = (String) idToken.getPayload().get("name");

        User user = new User();
        user.setGoogleId(googleId);
        user.setEmail(email);
        user.setName(name);
        user.setUsername(email);
        user.setRole(0);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }

    private GoogleIdToken getGoogleIdToken(String idToken) {
        try {
            return verifier.verify(idToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new NotAuthenticated("Invalid ID token: " + e.getMessage());
        }
    }
}
