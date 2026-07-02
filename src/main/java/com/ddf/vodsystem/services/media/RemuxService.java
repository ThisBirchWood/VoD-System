package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.dto.CommandOutput;
import com.ddf.vodsystem.dto.ProgressTracker;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RemuxService {
    private final CommandRunner commandRunner;

    public RemuxService(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    @Async("ffmpegTaskExecutor")
    public CompletableFuture<CommandOutput> remux(File inputFile,
                                                  File outputFile,
                                                  ProgressTracker remuxProgress,
                                                  float length
    ) throws IOException, InterruptedException {
        List<String> command = List.of(
                "ffmpeg",
                "-progress", "pipe:1",
                "-y",
                "-i", inputFile.getAbsolutePath(),
                "-c:v", "h264",
                "-c:a", "aac",
                "-f", "mp4",
                outputFile.getAbsolutePath()
        );

        return CompletableFuture.completedFuture(commandRunner.run(command, line ->
                commandRunner.setProgress(line, remuxProgress, length)));
    }
}
