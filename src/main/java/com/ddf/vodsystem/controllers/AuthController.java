package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.entities.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/")
public class AuthController {

    @GetMapping("/user")
    public ResponseEntity<APIResponse<Map<String, Object>>> user(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).
                    body(new APIResponse<>(
                            "error", 
                            "User not authenticated", 
                            null
                    ));
        }

        return ResponseEntity.ok(
                new APIResponse<>("success", "User details retrieved successfully", Map.of(
                        "name", principal.getAttribute("name"),
                        "email", principal.getAttribute("email"))
                )
        );
    }
}