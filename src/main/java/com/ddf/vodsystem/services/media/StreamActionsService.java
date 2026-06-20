package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.services.DirectoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
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
            List<File> segments,
            float trimOffset,
            float duration,
            File outputFile
    ) throws IOException, InterruptedException {
        String concatInput = segments.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.joining("|"));

        List<String> command = List.of(
                "ffmpeg",
                "-y",
                "-i", "concat:" + concatInput,
                "-ss", String.valueOf(trimOffset),
                "-t", String.valueOf(duration),
                "-c", "copy",
                outputFile.getAbsolutePath()
        );

        logger.info("Saving section with ({} segments) to '{}'",
                segments.size(), outputFile.getAbsolutePath());
        CommandRunner.run(command, line -> logger.debug("{}", line));

        return CompletableFuture.completedFuture(outputFile);
    }

}
