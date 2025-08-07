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

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long) {
            return getUserById((long) auth.getPrincipal());
        }
        return null;
    }

    public String login(String idToken) {
        User user = parseIdToken(idToken);

        if (user == null) {
            throw new NotAuthenticated("Invalid ID token");
        }
        Optional<User> existingUser = userRepository.findByGoogleId(user.getGoogleId());

        if (existingUser.isEmpty()) {
            userRepository.saveAndFlush(user);
        }

        return jwtService.generateToken(user.getId());
    }

    private User parseIdToken(String idToken) {
        try {
            GoogleIdToken idTokenObject = verifier.verify(idToken);

            if (idTokenObject == null) {
                return null;
            }

            GoogleIdToken.Payload payload = idTokenObject.getPayload();
            String name = (String) payload.get("name");
            String email = payload.getEmail();
            String googleId = payload.getSubject();

            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setGoogleId(googleId);
            user.setUsername(email);
            user.setRole(0); // Default role for new users
            user.setCreatedAt(java.time.LocalDateTime.now());
            return user;
        } catch (Exception e) {
            return null;
        }
    }
}
