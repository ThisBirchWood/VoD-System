package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.controllers.dto.ClipResponse;
import com.ddf.vodsystem.dto.APIResponse;
import com.ddf.vodsystem.controllers.dto.ClipUpdateRequest;
import com.ddf.vodsystem.entities.Clip;
import com.ddf.vodsystem.services.ClipService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clips")
public class ClipController {
    private final ClipService clipService;
    private static final String SUCCESS = "success";
    private static final String FILENAME_HEADER = "inline; filename=\"%s\"";

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
        Clip clip = clipService.getClipById(id);
        ClipResponse clipDTO = convertToDTO(clip);

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
        clipService.deleteClip(id);

        return ResponseEntity.ok(
                new APIResponse<>(
                        SUCCESS,
                        "Clip deleted successfully",
                        "Clip with ID " + id + " has been deleted"
                )
        );
    }

    @GetMapping("/{id}/media")
    public ResponseEntity<?> downloadClip(@PathVariable Long id, @RequestHeader HttpHeaders headers) throws IOException {
        Resource resource = clipService.downloadClip(id);

        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        List<HttpRange> ranges = headers.getRange();

        if (!ranges.isEmpty()) {
            ResourceRegion region = createResourceRegion(resource, ranges.getFirst());

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format(FILENAME_HEADER, resource.getFilename()))
                    .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                    .body(region);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format(FILENAME_HEADER, resource.getFilename()))
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(resource);
    }

    private ResourceRegion createResourceRegion(Resource resource, HttpRange range) throws IOException {
        long contentLength = resource.contentLength();
        long start = range.getRangeStart(contentLength);
        long end = range.getRangeEnd(contentLength);
        return new ResourceRegion(resource, start, end - start + 1);
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<Resource> downloadThumbnail(@PathVariable Long id) {
        Resource resource = clipService.downloadThumbnail(id);

        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format(FILENAME_HEADER, resource.getFilename()))
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(resource);
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
