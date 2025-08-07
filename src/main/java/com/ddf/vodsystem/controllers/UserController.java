package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.dto.APIResponse;
import com.ddf.vodsystem.dto.TokenDTO;
import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public ResponseEntity<APIResponse<User>> user() {
        User user = userService.getCurrentUser();

        if (user == null) {
            throw new NotAuthenticated("User not authenticated");
        }

        return ResponseEntity.ok(
                new APIResponse<>("success", "User retrieved successfully", user)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<TokenDTO>> login(@RequestBody TokenDTO tokenDTO) {
        String jwt = userService.login(tokenDTO.getToken());

        return ResponseEntity.ok(
                new APIResponse<>("success", "Logged in successfully", new TokenDTO(jwt))
        );
    }
}