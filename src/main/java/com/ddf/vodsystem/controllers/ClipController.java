package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.dto.ClipDTO;
import com.ddf.vodsystem.dto.APIResponse;
import com.ddf.vodsystem.entities.Clip;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.services.ClipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/clips")
public class ClipController {
    private final ClipService clipService;

    public ClipController(ClipService clipService) {
        this.clipService = clipService;
    }

    @GetMapping("")
    public ResponseEntity<APIResponse<List<ClipDTO>>> getClips() {
        List<Clip> clips = clipService.getClipsByUser();
        List<ClipDTO> clipDTOs = clips.stream()
                .map(this::convertToDTO)
                .toList();

        return ResponseEntity.ok(
                new APIResponse<>("success", "Clips retrieved successfully", clipDTOs)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<ClipDTO>> getClipById(@PathVariable Long id) {
        Optional<Clip> clip = clipService.getClipById(id);
        if (clip.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ClipDTO clipDTO = convertToDTO(clip.get());

        return ResponseEntity.ok(
                new APIResponse<>("success", "Clip retrieved successfully", clipDTO)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> deleteClip(@AuthenticationPrincipal OAuth2User principal, @PathVariable Long id) {
        if (principal == null) {
            throw new NotAuthenticated("User is not authenticated");
        }

        boolean deleted = clipService.deleteClip(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                new APIResponse<>("success", "Clip deleted successfully", "Clip with ID " + id + " has been deleted")
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
