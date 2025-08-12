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

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public String login(String idToken) {
        GoogleIdToken googleIdToken = getGoogleIdToken(idToken);
        String googleId = googleIdToken.getPayload().getSubject();

        if (googleId == null) {
            throw new NotAuthenticated("Invalid ID token");
        }

        User googleUser = getGoogleUser(googleIdToken);
        User user = createOrUpdateUser(googleUser);

        return jwtService.generateToken(user.getId());
    }

    private User createOrUpdateUser(User user) {
        Optional<User> existingUser = userRepository.findByGoogleId(user.getGoogleId());

        if (existingUser.isEmpty()) {
            user.setRole(0);
            user.setCreatedAt(LocalDateTime.now());
            return userRepository.saveAndFlush(user);
        }

        User existing = existingUser.get();
        existing.setEmail(user.getEmail());
        existing.setName(user.getName());
        existing.setProfilePictureUrl(user.getProfilePictureUrl());
        existing.setUsername(user.getUsername());
        return userRepository.saveAndFlush(existing);
    }

    private User getGoogleUser(GoogleIdToken idToken) {
        String googleId = idToken.getPayload().getSubject();
        String email = idToken.getPayload().getEmail();
        String name = (String) idToken.getPayload().get("name");
        String profilePictureUrl = (String) idToken.getPayload().get("picture");

        User user = new User();
        user.setGoogleId(googleId);
        user.setEmail(email);
        user.setName(name);
        user.setUsername(email);
        user.setProfilePictureUrl(profilePictureUrl);

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
