package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.dto.ClipDTO;
import com.ddf.vodsystem.dto.APIResponse;
import com.ddf.vodsystem.entities.Clip;
import com.ddf.vodsystem.services.ClipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/clips")
public class ClipController {
    private final ClipService clipService;
    private static final String SUCCESS = "success";

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
                new APIResponse<>(SUCCESS,
                        "Clips retrieved successfully",
                        clipDTOs
                )
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
                new APIResponse<>(SUCCESS,
                        "Clip retrieved successfully",
                        clipDTO
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> deleteClip(@PathVariable Long id) {
        if (!clipService.deleteClip(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                new APIResponse<>(
                        SUCCESS,
                        "Clip deleted successfully",
                        "Clip with ID " + id + " has been deleted"
                )
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
