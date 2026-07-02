package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.controllers.dto.MarkerCreateRequest;
import com.ddf.vodsystem.controllers.dto.MarkerResponse;
import com.ddf.vodsystem.dto.APIResponse;
import com.ddf.vodsystem.entities.Marker;
import com.ddf.vodsystem.services.MarkerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/markers")
public class MarkerController {
    private static final String SUCCESS = "success";

    private final MarkerService markerService;

    public MarkerController(MarkerService markerService) {
        this.markerService = markerService;
    }

    @GetMapping("")
    public ResponseEntity<APIResponse<List<MarkerResponse>>> getMarkers() {
        List<MarkerResponse> markers = markerService.getUserMarkers().stream()
                .map(this::convertToDTO)
                .toList();

        return ResponseEntity.ok(
                new APIResponse<>(SUCCESS, "Markers retrieved successfully", markers)
        );
    }

    @PostMapping("")
    public ResponseEntity<APIResponse<MarkerResponse>> createMarker(@Valid @RequestBody MarkerCreateRequest request) {
        Marker marker = markerService.create(request.message());

        return ResponseEntity.ok(
                new APIResponse<>(SUCCESS, "Marker created successfully", convertToDTO(marker))
        );
    }

    private MarkerResponse convertToDTO(Marker marker) {
        return new MarkerResponse(
                marker.getId(),
                marker.getUser().getId(),
                marker.getStream().getId(),
                marker.getMessage(),
                marker.getTimestamp()
        );
    }
}
