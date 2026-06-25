package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.controllers.dto.StreamResponse;
import com.ddf.vodsystem.dto.APIResponse;
import com.ddf.vodsystem.entities.Stream;
import com.ddf.vodsystem.services.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/stream")
public class StreamController {
    private static final Logger logger = LoggerFactory.getLogger(StreamController.class);
    private static final String SUCCESS = "success";

    private final StreamService streamService;

    public StreamController(StreamService streamService) {
        this.streamService = streamService;
    }

    // Called by nginx on_publish
    @PostMapping("/start")
    public ResponseEntity<APIResponse<Stream>> startStream(@RequestParam("name") String streamKey) {
        logger.info("Stream start callback for key: {}", streamKey);
        Stream stream = streamService.startStream(streamKey);
        return ResponseEntity.ok(new APIResponse<>(SUCCESS, "Stream started", stream));
    }

    // Called by nginx on_publish_done
    @PostMapping("/stop")
    public ResponseEntity<APIResponse<Void>> stopStream(@RequestParam("name") String streamKey) {
        logger.info("Stream stop callback for key: {}", streamKey);
        streamService.endStream(streamKey);
        return ResponseEntity.ok(new APIResponse<>(SUCCESS, "Stream ended", null));
    }

    // Called by nginx on_update
    @PostMapping("/heartbeat")
    public ResponseEntity<APIResponse<Void>> heartbeat(@RequestParam("name") String streamKey) {
        streamService.heartbeatStream(streamKey);
        return ResponseEntity.ok(new APIResponse<>(SUCCESS, "Heartbeat recorded", null));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<APIResponse<List<Stream>>> getStreamHistory(@PathVariable Long userId) {
        List<Stream> history = streamService.getStreamHistory(userId);
        return ResponseEntity.ok(new APIResponse<>(SUCCESS, "Stream history retrieved", history));
    }

    @GetMapping("/current")
    public ResponseEntity<APIResponse<StreamResponse>> getCurrentStream() {
        Optional<Stream> stream = streamService.getActiveStream();
        return stream.map(value -> ResponseEntity.ok(new APIResponse<>(SUCCESS,
                "Streaming status retrieved",
                new StreamResponse(true, value.getId())))).orElseGet(() -> ResponseEntity.ok(new APIResponse<>(SUCCESS,
                "Streaming status retrieved",
                new StreamResponse(false, null))));
    }
}
