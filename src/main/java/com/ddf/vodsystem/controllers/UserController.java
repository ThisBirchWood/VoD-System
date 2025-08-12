package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.dto.APIResponse;
import com.ddf.vodsystem.dto.TokenDTO;
import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth/")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public ResponseEntity<APIResponse<User>> user() {
        Optional<User> user = userService.getLoggedInUser();

        if (user.isEmpty()) {
            throw new NotAuthenticated("User not authenticated");
        }

        return ResponseEntity.ok(
                new APIResponse<>("success", "User retrieved successfully", user.get())
        );
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<TokenDTO>> login(@RequestBody TokenDTO tokenDTO,
                                                       HttpServletResponse response) {
        String jwt = userService.login(tokenDTO.getToken());

        ResponseCookie cookie = ResponseCookie.from("token", jwt)
                .httpOnly(true)
                .maxAge(60 * 60 * 24)
                .sameSite("None")
                .secure(true)
                .path("/")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(
                new APIResponse<>("success", "Logged in successfully", new TokenDTO(jwt))
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<Void>> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .maxAge(0)
                .sameSite("None")
                .secure(true)
                .path("/")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(
                new APIResponse<>("success", "Logged out successfully", null)
        );
    }
}