package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.dto.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service responsible for performing stream-related media actions, such as
 * extracting and saving a trimmed section of video from a sequence of
 * recorded segments.
 * <p>
 * Operations in this service are backed by {@code ffmpeg} and run
 * asynchronously on a dedicated executor so that potentially long-running
 * encoding/concatenation work does not block request-handling threads.
 */
@Service
public class StreamActionsService {
    private static final Logger logger = LoggerFactory.getLogger(StreamActionsService.class);
    private final CommandRunner commandRunner;

    private static final int HLS_SEGMENT_LENGTH = 3;

    public StreamActionsService(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    /**
     * Concatenates a list of video segments and extracts a trimmed section
     * from the result, writing the output to {@code outputFile}.
     * <p>
     * Failures (process launch errors, ffmpeg failures, interruption) are
     * surfaced via the returned future rather than thrown, in line with
     * normal {@link CompletableFuture} conventions — callers should use
     * {@code .exceptionally()} / {@code .handle()} rather than a try/catch.
     *
     * @param segments    the ordered list of segment files to concatenate,
     *                    in playback order
     * @param trimOffset  the offset, in seconds, from the start of the
     *                    concatenated stream at which to begin the trimmed
     *                    section
     * @param duration    the duration, in seconds, of the trimmed section
     *                    to extract
     * @param outputFile  the destination file the trimmed section will be
     *                    written to; overwritten if it already exists
     * @return a {@link CompletableFuture} that completes with
     *         {@code outputFile} on success, or completes exceptionally on
     *         failure
     */
    @Async("ffmpegTaskExecutor")
    public CompletableFuture<Path> saveSection(
            List<Path> segments,
            float trimOffset,
            float duration,
            Path outputFile,
            ProgressTracker progressTracker
    ) {
        try {
            String concatInput = segments.stream()
                    .map(p -> p.toAbsolutePath().toString())
                    .collect(Collectors.joining("|"));

            List<String> command = List.of(
                    "ffmpeg",
                    "-y",
                    "-i", "concat:" + concatInput,
                    "-ss", String.valueOf(trimOffset),
                    "-t", String.valueOf(duration),
                    "-c", "copy",
                    outputFile.toAbsolutePath().toString()
            );

            logger.info("Saving section with ({} segments) to '{}'",
                    segments.size(), outputFile);
            commandRunner.run(command, line -> commandRunner.setProgress(line, progressTracker, duration));
            progressTracker.markComplete();

            return CompletableFuture.completedFuture(outputFile);
        } catch (Exception e) {
            logger.error("Failed to save section to '{}'", outputFile, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public List<Path> getSegmentsInRange(Path streamDirectory, Instant startTime, Instant endTime) throws IOException {
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

    public long parseTimestampMs(Path path) {
        String name = path.getFileName().toString().replace(".ts", "");
        long value = Long.parseLong(name);
        // nginx hls_fragment_naming system uses seconds; convert to ms
        return value < 1_000_000_000_000L ? value * 1000L : value;
    }
}