package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.dto.ClipResponse;
import com.ddf.vodsystem.dto.APIResponse;
import com.ddf.vodsystem.dto.ClipUpdateRequest;
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
    public ResponseEntity<APIResponse<List<ClipResponse>>> getClips() {
        List<Clip> clips = clipService.getClipsByUser();
        List<ClipResponse> clipDTOs = clips.stream()
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
    public ResponseEntity<APIResponse<ClipResponse>> getClipById(@PathVariable Long id) {
        Optional<Clip> clip = clipService.getClipById(id);
        if (clip.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ClipResponse clipDTO = convertToDTO(clip.get());

        return ResponseEntity.ok(
                new APIResponse<>(SUCCESS,
                        "Clip retrieved successfully",
                        clipDTO
                )
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<APIResponse<ClipResponse>> updateClip(@PathVariable Long id,
                                                           @RequestBody ClipUpdateRequest updateFields) {
        Clip clip = clipService.updateClip(id, updateFields);
        ClipResponse clipDTO = convertToDTO(clip);

        return ResponseEntity.ok(
                new APIResponse<>(SUCCESS, "Clip updated successfully", clipDTO)
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

    private ClipResponse convertToDTO(Clip clip) {
        return new ClipResponse(
                clip.getId(),
                clip.getUser().getId(),
                clip.getTitle(),
                clip.getDescription(),
                clip.getDuration(),
                clip.getCreatedAt()
        );
    }
}
