package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.dto.CommandOutput;
import com.ddf.vodsystem.dto.ProgressTracker;
import com.ddf.vodsystem.dto.options.ClipOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CompressionService {
    private static final Logger logger = LoggerFactory.getLogger(CompressionService.class);

    private static final float AUDIO_RATIO = 0.15f;
    private static final float MAX_AUDIO_BITRATE = 128f;
    private static final float BITRATE_MULTIPLIER = 0.9f;
    private final Pattern timePattern = Pattern.compile("out_time_ms=(\\d+)");

    @Async("ffmpegTaskExecutor")
    public CompletableFuture<CommandOutput> compress(File inputFile,
                                                     File outputFile,
                                                     ClipOptions clipOptions,
                                                     ProgressTracker progress
    ) throws IOException, InterruptedException {
        logger.info("Compressing video from {} to {}", inputFile.getAbsolutePath(), outputFile.getAbsolutePath());

        List<String> command = buildCommand(inputFile, outputFile, clipOptions);
        CommandOutput result = CommandRunner.run(command, line -> setProgress(line, progress, clipOptions.getDuration()));
        progress.markComplete();

        return CompletableFuture.completedFuture(result);
    }

    private void setProgress(String line, ProgressTracker progress, float length) {
        Matcher matcher = timePattern.matcher(line);
        if (matcher.find()) {
            float timeInMs = Float.parseFloat(matcher.group(1)) / 1000000f;
            progress.setProgress(timeInMs / length);
        }
    }

    private List<String> buildFilters(Float fps, Integer width, Integer height) {
        List<String> command = new ArrayList<>();
        command.add("-vf");

        List<String> filters = new ArrayList<>();

        if (fps != null) {
            logger.info("Frame rate set to {}", fps);
            filters.add("fps=" + fps);
        }

        if (!(width == null && height == null)) {
            logger.info("Scaling video to width: {}, height: {}", width, height);
            String w = (width != null) ? width.toString() : "-1";
            String h = (height != null) ? height.toString() : "-1";
            filters.add("scale=" + w + ":" + h);
        }

        logger.info("Adding video filters");
        command.add(String.join(",", filters));
        return command;
    }

    private List<String> buildBitrate(Float length, Float fileSize) {
        List<String> command = new ArrayList<>();

        float bitrate = ((fileSize * 8) / length) * BITRATE_MULTIPLIER;
        float audioBitrate = bitrate * AUDIO_RATIO;
        float videoBitrate;

        if (audioBitrate > MAX_AUDIO_BITRATE) {
            audioBitrate = MAX_AUDIO_BITRATE;
            videoBitrate = bitrate - MAX_AUDIO_BITRATE;
        } else {
            videoBitrate = bitrate * (1 - AUDIO_RATIO);
        }

        command.add("-b:v");
        command.add(videoBitrate + "k");
        command.add("-b:a");
        command.add(audioBitrate + "k");

        return command;
    }

    private List<String> buildInputs(File inputFile, Float startPoint, Float length) {
        List<String> command = new ArrayList<>();

        command.add("-ss");
        command.add(startPoint.toString());

        command.add("-i");
        command.add(inputFile.getAbsolutePath());

        command.add("-t");
        command.add(Float.toString(length));

        return command;
    }

    private List<String> buildCommand(File inputFile, File outputFile, ClipOptions clipOptions) {
        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-progress");
        command.add("pipe:1");
        command.add("-y");

        command.addAll(buildInputs(inputFile, clipOptions.getStartPoint(), clipOptions.getDuration()));

        if (clipOptions.getFps() != null || clipOptions.getWidth() != null || clipOptions.getHeight() != null) {
            command.addAll(buildFilters(clipOptions.getFps(), clipOptions.getWidth(), clipOptions.getHeight()));
        }

        if (clipOptions.getFileSize() != null) {
            command.addAll(buildBitrate(clipOptions.getDuration(), clipOptions.getFileSize()));
        }

        // Output file
        command.add(outputFile.getAbsolutePath());
        return command;
    }
}
