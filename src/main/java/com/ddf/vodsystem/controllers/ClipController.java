package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.dto.ClipDTO;
import com.ddf.vodsystem.dto.APIResponse;
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
    public ResponseEntity<APIResponse<List<ClipDTO>>> getClips(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            throw new NotAuthenticated("User is not authenticated");
        }

        List<Clip> clips = clipService.getClipsByUser();
        List<ClipDTO> clipDTOs = clips.stream()
                .map(this::convertToDTO)
                .toList();

        return ResponseEntity.ok(
                new APIResponse<>("success", "Clips retrieved successfully", clipDTOs)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<ClipDTO>> getClipById(@AuthenticationPrincipal OAuth2User principal, @PathVariable Long id) {
        if (principal == null) {
            throw new NotAuthenticated("User is not authenticated");
        }

        Clip clip = clipService.getClipById(id);
        if (clip == null) {
            return ResponseEntity.notFound().build();
        }

        ClipDTO clipDTO = convertToDTO(clip);

        return ResponseEntity.ok(
                new APIResponse<>("success", "Clip retrieved successfully", clipDTO)
        );
    }

    private ClipDTO convertToDTO(Clip clip) {
        ClipDTO dto = new ClipDTO();
        dto.setId(clip.getId());
        dto.setUserId(clip.getUser().getId());
        dto.setTitle(clip.getTitle());
        dto.setDescription(clip.getDescription());
        dto.setDuration(clip.getDuration());
        dto.setCreatedAt(clip.getCreatedAt());
        return dto;
    }
}
