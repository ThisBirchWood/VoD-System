package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.services.DirectoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class StreamActionsService {
    private static final Logger logger = LoggerFactory.getLogger(StreamActionsService.class);

    private final DirectoryService directoryService;

    public StreamActionsService(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    @Async("ffmpegTaskExecutor")
    public CompletableFuture<File> saveSection(
            String streamDirectory,
            Instant startTime,
            Instant endTime
    ) throws IOException, InterruptedException {
        List<File> segments = getSegmentsInRange(streamDirectory, startTime, endTime);
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("No segments found in range [" + startTime + ", " + endTime + "]");
        }

        long firstSegmentMs = parseTimestampMs(segments.get(0));
        float trimOffset = Math.max(0f, (startTime.toEpochMilli() - firstSegmentMs) / 1000f);
        float duration = (endTime.toEpochMilli() - startTime.toEpochMilli()) / 1000f;

        String concatInput = segments.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.joining("|"));

        File outputFile = directoryService.getVodFile(UUID.randomUUID() + ".mp4");

        List<String> command = List.of(
                "ffmpeg",
                "-y",
                "-i", "concat:" + concatInput,
                "-ss", String.valueOf(trimOffset),
                "-t", String.valueOf(duration),
                "-c", "copy",
                outputFile.getAbsolutePath()
        );

        logger.info("Saving section [{} - {}] from '{}' ({} segments) to '{}'",
                startTime, endTime, streamDirectory, segments.size(), outputFile.getAbsolutePath());
        CommandRunner.run(command, line -> logger.debug("{}", line));

        return CompletableFuture.completedFuture(outputFile);
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
