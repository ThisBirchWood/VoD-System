package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.entities.APIResponse;
import com.ddf.vodsystem.entities.Clip;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.services.ClipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/v1/clips")
public class ClipController {
    private final ClipService clipService;

    public ClipController(ClipService clipService) {
        this.clipService = clipService;
    }

    @GetMapping("/")
    public ResponseEntity<APIResponse<List<Clip>>> getClips(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            throw new NotAuthenticated("User is not authenticated");
        }

        List<Clip> clips = clipService.getClipsByUser();
        return ResponseEntity.ok(
                new APIResponse<>("success", "Clips retrieved successfully", clips)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<Clip>> getClipById(@AuthenticationPrincipal OAuth2User principal, @PathVariable Long id) {
        if (principal == null) {
            throw new NotAuthenticated("User is not authenticated");
        }

        Clip clip = clipService.getClipById(id);
        if (clip == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                new APIResponse<>("success", "Clip retrieved successfully", clip)
        );
    }
}
