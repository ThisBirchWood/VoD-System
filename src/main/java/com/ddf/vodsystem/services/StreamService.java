package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.Stream;
import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.exceptions.AlreadyStreaming;
import com.ddf.vodsystem.exceptions.FFMPEGException;
import com.ddf.vodsystem.exceptions.KeyNotFound;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.repositories.StreamRepository;
import com.ddf.vodsystem.services.media.StreamActionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StreamService {
    private static final Logger logger = LoggerFactory.getLogger(StreamService.class);

    private static final int HEARTBEAT_TIMEOUT_SECONDS = 60;
    private static final float CLIP_MAX_LENGTH = 180;

    private final StreamRepository streamRepository;
    private final UserService userService;
    private final StreamActionsService streamActionsService;

    @Value("${storage.stream}")
    private String streamDataPath;

    public StreamService(StreamRepository streamRepository,
                         UserService userService,
                         StreamActionsService streamActionsService) {
        this.streamRepository = streamRepository;
        this.userService = userService;
        this.streamActionsService = streamActionsService;
    }

    public Stream startStream(String streamKey) {
        User user = resolveUser(streamKey);

        streamRepository.findByUserAndEndDateIsNull(user).ifPresent(existing -> {
            throw new AlreadyStreaming("User " + user.getUsername() + " is already streaming.");
        });

        LocalDateTime now = LocalDateTime.now();
        Stream stream = new Stream();
        stream.setUser(user);
        stream.setStartDate(now);
        stream.setLastSeen(now);
        return streamRepository.saveAndFlush(stream);
    }

    public void endStream(String streamKey) {
        User user = resolveUser(streamKey);

        Stream stream = streamRepository.findByUserAndEndDateIsNull(user)
                .orElseThrow(() -> new IllegalStateException(
                        "No active stream found for user " + user.getUsername()));

        stream.setEndDate(LocalDateTime.now());
        streamRepository.saveAndFlush(stream);
    }

    public void heartbeatStream(String streamKey) {
        User user = resolveUser(streamKey);

        Stream stream = streamRepository.findByUserAndEndDateIsNull(user)
                .orElseThrow(() -> new IllegalStateException(
                        "No active stream found for user " + user.getUsername()));

        stream.setLastSeen(LocalDateTime.now());
        streamRepository.saveAndFlush(stream);
    }

    public Optional<Stream> getActiveStream(String streamKey) {
        User user = resolveUser(streamKey);
        return streamRepository.findByUserAndEndDateIsNull(user);
    }

    public List<Stream> getStreamHistory(Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return streamRepository.findByUser(user);
    }

    public void saveSection(Instant startTime, Instant endTime) throws IOException, InterruptedException {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        User user = userService.getLoggedInUser()
                .orElseThrow(() -> new NotAuthenticated("User is not authenticated"));

        String streamDirectory = streamDataPath + File.separator + user.getStreamKey();
        List<File> fileSegments = getSegmentsInRange(streamDirectory, startTime, endTime);

        if (fileSegments.isEmpty()) {
            throw new IllegalArgumentException("No stream segments found in the given time range");
        }

        long firstSegmentMs = parseTimestampMs(fileSegments.getFirst());
        float trimOffset = Math.max(0f, (startTime.toEpochMilli() - firstSegmentMs) / 1000f);
        float duration = (endTime.toEpochMilli() - startTime.toEpochMilli()) / 1000f;

        streamActionsService.saveSection(fileSegments, trimOffset, duration)
                .exceptionally(ex -> {
                    throw new FFMPEGException("Saving stream section failed due to FFMPEG Error");
                });
    }

    public void clip(float duration) throws IOException, InterruptedException {
        if (duration <= 0 || duration > CLIP_MAX_LENGTH) {
            throw new IllegalArgumentException("Clip length must be between 0 and " + CLIP_MAX_LENGTH + " seconds");
        }

        User user = userService.getLoggedInUser()
                .orElseThrow(() -> new NotAuthenticated("User is not authenticated"));

        Instant endTime = Instant.now();
        // minusSeconds() is possible, but only does integer seconds, not float
        Instant startTime = Instant.now().minus(Duration.ofMillis((long) (duration * 1000)));

        String streamDirectory = streamDataPath + File.separator + user.getStreamKey();
        List<File> fileSegments = getSegmentsInRange(streamDirectory, startTime, endTime);

        if (fileSegments.isEmpty()) {
            throw new IllegalArgumentException("No stream segments found in the given time range");
        }

        long firstSegmentMs = parseTimestampMs(fileSegments.getFirst());
        float trimOffset = Math.max(0f, (startTime.toEpochMilli() - firstSegmentMs) / 1000f);

        streamActionsService.saveSection(fileSegments, trimOffset, duration)
                .exceptionally(ex -> {
                    throw new FFMPEGException("Saving stream section failed due to FFMPEG Error");
                });
    }

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void endStaleStreams() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(HEARTBEAT_TIMEOUT_SECONDS);
        List<Stream> stale = streamRepository.findByEndDateIsNullAndLastSeenBefore(cutoff);
        if (stale.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        for (Stream stream : stale) {
            stream.setEndDate(now);
            logger.warn("Stream {} for user {} ended due to heartbeat timeout", stream.getId(), stream.getUser().getUsername());
        }
        streamRepository.saveAllAndFlush(stale);
    }

    private User resolveUser(String streamKey) {
        return userService.getUserByStreamKey(streamKey)
                .orElseThrow(() -> new KeyNotFound("Stream key not found: " + streamKey));
    }

    private List<File> getSegmentsInRange(String streamDirectory, Instant startTime, Instant endTime) {
        File dir = new File(streamDirectory);
        File[] tsFiles = dir.listFiles((d, name) -> name.endsWith(".ts"));
        if (tsFiles == null) return List.of();

        long startMs = startTime.toEpochMilli();
        long endMs = endTime.toEpochMilli();

        return Arrays.stream(tsFiles)
                .filter(f -> {
                    long segMs = parseTimestampMs(f);
                    return segMs < endMs && segMs + 3_000 > startMs;
                })
                .sorted(Comparator.comparingLong(this::parseTimestampMs))
                .collect(Collectors.toList());
    }

    private long parseTimestampMs(File file) {
        String name = file.getName().replace(".ts", "");
        long value = Long.parseLong(name);
        // nginx hls_fragment_naming system uses seconds; convert to ms
        return value < 1_000_000_000_000L ? value * 1000L : value;
    }
}
