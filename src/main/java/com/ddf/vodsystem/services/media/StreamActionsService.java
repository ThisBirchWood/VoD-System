package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.dto.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
            CommandRunner.run(command, line -> CommandRunner.setProgress(line, progressTracker, duration));

            return CompletableFuture.completedFuture(outputFile);
        } catch (Exception e) {
            logger.error("Failed to save section to '{}'", outputFile, e);
            return CompletableFuture.failedFuture(e);
        }
    }
}