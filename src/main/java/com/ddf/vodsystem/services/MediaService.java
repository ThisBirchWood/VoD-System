package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.ClipOptions;
import com.ddf.vodsystem.dto.Job;
import com.ddf.vodsystem.dto.JobState;
import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.services.media.CompressionService;
import com.ddf.vodsystem.services.media.StreamActionsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MediaService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MediaService.class);
    private final JobRegistryService jobRegistryService;
    private final DirectoryService directoryService;
    private final CompressionService compressionService;
    private final UserService userService;
    private final ClipService clipService;

    private static final float CLIP_MAX_LENGTH = 180;
    private static final int HLS_SEGMENT_LENGTH = 3;
    private final StreamActionsService streamActionsService;
    private final VodService vodService;

    public MediaService(JobRegistryService jobRegistryService,
                        DirectoryService directoryService,
                        CompressionService compressionService,
                        UserService userService,
                        ClipService clipService, StreamActionsService streamActionsService, VodService vodService) {
        this.jobRegistryService = jobRegistryService;
        this.directoryService = directoryService;
        this.compressionService = compressionService;
        this.userService = userService;
        this.clipService = clipService;
        this.streamActionsService = streamActionsService;
        this.vodService = vodService;
    }

    public Job compress(MultipartFile file, ClipOptions clipOptions) throws IOException, InterruptedException {
        Job job = jobRegistryService.generateJob();

        Optional<User> user = userService.getLoggedInUser();
        String filename = job.getUuid() + ".mp4";

        Path tempInputFile = directoryService.getTempInputDir().resolve(filename);
        Path tempOutputFile = directoryService.getTempOutputDir().resolve(filename);
        directoryService.saveMultipartFile(tempInputFile, file);

        job.setState(JobState.PROCESSING);
        compressionService.compress(
                tempInputFile,
                tempOutputFile,
                clipOptions,
                job.getProgressTracker()
        ).thenRun(() -> {
            job.setDownload(tempOutputFile);
            job.setState(JobState.SUCCEEDED);
            logger.info("Compression job {} succeeded", job.getUuid());

            // save clip if user context
            user.ifPresent(userVal ->
                clipService.persistClip(
                        clipOptions.getTitle(),
                        clipOptions.getDescription(),
                        userVal,
                        tempOutputFile,
                        filename
                )
            );
        }).exceptionally(ex -> {
            job.setState(JobState.FAILED);
            job.setErrorOutput(ex.getMessage());
            logger.error("Compression job with UUID {} failed due to: {}", job.getUuid(), ex.getMessage());
            return null;
        });

        return job;
    }

    /**
     * Saves a clip of the currently authenticated user's stream between two points in time.
     * <p>
     * Locates the recorded {@code .ts} segments overlapping the requested window, computes the
     * trim offset into the first (partially-overlapping) segment and the total clip duration,
     * then delegates the actual extraction to {@link StreamActionsService#saveSection}.
     *
     * @param startTime the inclusive start of the section to save; must be before {@code endTime}
     * @param endTime   the exclusive end of the section to save
     * @throws IllegalArgumentException if {@code startTime} is not before {@code endTime},
     *                                  or if no stream segments exist in the given range
     * @throws NotAuthenticated         if no user is currently authenticated
     * @throws IOException              if reading the stream directory or its segments fails
     */
    public Job saveSection(Instant startTime, Instant endTime) throws IOException {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        User user = userService.getLoggedInUser()
                .orElseThrow(() -> new NotAuthenticated("User is not authenticated"));

        Job job = jobRegistryService.generateJob();
        Path streamDirectory = directoryService.getStreamDir(user.getStreamKey());
        List<Path> fileSegments = getSegmentsInRange(streamDirectory, startTime, endTime);

        if (fileSegments.isEmpty()) {
            throw new IllegalArgumentException("No stream segments found in the given time range");
        }

        long firstSegmentMs = parseTimestampMs(fileSegments.getFirst());
        float trimOffset = Math.max(0f, (startTime.toEpochMilli() - firstSegmentMs) / 1000f);
        float duration = (endTime.toEpochMilli() - startTime.toEpochMilli()) / 1000f;

        job.setState(JobState.PROCESSING);
        Path outputFile = directoryService.getVodsDir(user.getId()).resolve(UUID.randomUUID() + ".mp4");
        streamActionsService.saveSection(
            fileSegments,
            trimOffset,
            duration,
            outputFile,
            job.getProgressTracker()
            ).thenRun(() -> {
                job.setDownload(outputFile);
                job.setState(JobState.SUCCEEDED);
                logger.info("Stream save of job {} succeeded", job.getUuid());
                vodService.persist(
                        Instant.now().toString(),
                        "",
                        user,
                        outputFile,
                        outputFile.getFileName().toString()
                );

            }).exceptionally(ex -> {
                job.setState(JobState.FAILED);
                job.setErrorOutput(ex.getMessage());
                logger.error("Stream section save job with UUID {} failed due to: {}", job.getUuid(), ex.getMessage());
                return null;
            });

        return job;
    }

    /**
     * Saves a clip of the last {@code duration} seconds of the currently authenticated user's stream.
     * <p>
     * Computes a time window ending at the current instant and beginning {@code duration} seconds
     * earlier (with millisecond precision), then delegates to {@link #saveSection(Instant, Instant)}.
     *
     * @param duration the length of the clip in seconds, measured back from now; must be greater
     *                 than {@code 0} and no greater than {@link #CLIP_MAX_LENGTH}. Fractional values
     *                 are supported and resolved to the millisecond.
     * @throws IllegalArgumentException if {@code duration} is not in the range {@code (0, CLIP_MAX_LENGTH]},
     *                                  or if no stream segments exist in the computed window
     * @throws NotAuthenticated         if no user is currently authenticated
     * @throws IOException              if reading the stream directory or its segments fails
     */
    public Job clip(float duration) throws IOException {
        if (duration <= 0 || duration > CLIP_MAX_LENGTH) {
            throw new IllegalArgumentException("Clip length must be between 0 and " + CLIP_MAX_LENGTH + " seconds");
        }

        // minusSeconds() is possible, but only does integer seconds, not float
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(Duration.ofMillis((long) (duration * 1000)));

        User user = userService.getLoggedInUser()
                .orElseThrow(() -> new NotAuthenticated("User is not authenticated"));

        Job job = jobRegistryService.generateJob();
        Path streamDirectory = directoryService.getStreamDir(user.getStreamKey());
        List<Path> fileSegments = getSegmentsInRange(streamDirectory, startTime, endTime);

        if (fileSegments.isEmpty()) {
            throw new IllegalArgumentException("No stream segments found in the given time range");
        }

        long firstSegmentMs = parseTimestampMs(fileSegments.getFirst());
        float trimOffset = Math.max(0f, (startTime.toEpochMilli() - firstSegmentMs) / 1000f);

        Path outputFile = directoryService.getTempOutputDir().resolve(UUID.randomUUID() + ".mp4");
        streamActionsService.saveSection(fileSegments, trimOffset, duration, outputFile, job.getProgressTracker())
            .thenRun(() -> {
                job.setDownload(outputFile);
                job.setState(JobState.SUCCEEDED);
                logger.info("Stream clip of job {} succeeded", job.getUuid());
                clipService.persistClip(
                        Instant.now().toString(),
                        "",
                        user,
                        outputFile,
                        outputFile.getFileName().toString()
                    );
                }
            ).exceptionally(ex -> {
                job.setState(JobState.FAILED);
                job.setErrorOutput(ex.getMessage());
                logger.error("Clip save job with UUID {} failed due to: {}", job.getUuid(), ex.getMessage());
                return null;
            });

        return job;
    }

    private List<Path> getSegmentsInRange(Path streamDirectory, Instant startTime, Instant endTime) throws IOException {
        long startMs = startTime.toEpochMilli();
        long endMs = endTime.toEpochMilli();

        try (Stream<Path> files = Files.list(streamDirectory)) {
            return files
                    .filter(p -> p.getFileName().toString().endsWith(".ts"))
                    .filter(p -> {
                        long segMs = parseTimestampMs(p);
                        return segMs < endMs && (segMs + HLS_SEGMENT_LENGTH * 1000) > startMs;
                    })
                    .sorted(Comparator.comparingLong(this::parseTimestampMs))
                    .collect(Collectors.toList());
        }
    }

    private long parseTimestampMs(Path path) {
        String name = path.getFileName().toString().replace(".ts", "");
        long value = Long.parseLong(name);
        // nginx hls_fragment_naming system uses seconds; convert to ms
        return value < 1_000_000_000_000L ? value * 1000L : value;
    }
}
