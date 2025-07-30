package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.dto.CommandOutput;
import com.ddf.vodsystem.dto.ProgressTracker;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RemuxService {
    private final Pattern timePattern = Pattern.compile("out_time_ms=(\\d+)");

    public CommandOutput remux(File inputFile,
                               File outputFile,
                               ProgressTracker remuxProgress,
                               float length
    ) throws IOException, InterruptedException {
        List<String> command = List.of(
                "ffmpeg",
                "-i", inputFile.getAbsolutePath(),
                "-c:v", "h264",
                "-c:a", "aac",
                "-f", "mp4",
                outputFile.getAbsolutePath()
        );

        return CommandRunner.run(command, line -> setProgress(line, remuxProgress, length));
    }

    private void setProgress(String line, ProgressTracker progress, float length) {
        Matcher matcher = timePattern.matcher(line);
        if (matcher.find()) {
            float timeInMs = Float.parseFloat(matcher.group(1)) / 1000000f;
            progress.setProgress(timeInMs / length);
        }
    }
}
