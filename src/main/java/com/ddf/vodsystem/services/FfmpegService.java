package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.ProgressTracker;
import com.ddf.vodsystem.dto.VideoMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FfmpegService {
    private static final Logger logger = LoggerFactory.getLogger(FfmpegService.class);

    private static final float AUDIO_RATIO = 0.15f;
    private static final float MAX_AUDIO_BITRATE = 128f;
    private static final float BITRATE_MULTIPLIER = 0.9f;
    private static final String FFMPEG_COMMAND = "ffmpeg";
    private final Pattern timePattern = Pattern.compile("out_time_ms=(\\d+)");

    public void runWithProgress(File inputFile, File outputFile, VideoMetadata videoMetadata, ProgressTracker progress) throws IOException, InterruptedException {
        logger.info("Starting FFMPEG process");

        List<String> command = buildCommand(inputFile, outputFile, videoMetadata);

        String strCommand = String.join(" ", command);
        logger.info("FFMPEG command: {}", strCommand);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        logger.info("FFMPEG process started with PID: {}", process.pid());

        updateJobProgress(process, progress, videoMetadata.getEndPoint() - videoMetadata.getStartPoint());
        process.waitFor();

        logger.info("FFMPEG process completed successfully");
    }

    public void run(File inputFile, File outputFile, VideoMetadata videoMetadata) throws IOException, InterruptedException {
        runWithProgress(inputFile, outputFile, videoMetadata, new ProgressTracker(0.0f));
    }

    public void generateThumbnail(File inputFile, File outputFile, Float timeInVideo) throws IOException, InterruptedException {
        logger.info("Generating thumbnail at {} seconds", timeInVideo);

        List<String> command = new ArrayList<>();
        command.add(FFMPEG_COMMAND);
        command.add("-ss");
        command.add(timeInVideo.toString());
        command.add("-i");
        command.add(inputFile.getAbsolutePath());
        command.add("-frames:v");
        command.add("1");
        command.add(outputFile.getAbsolutePath());

        String strCommand = String.join(" ", command);
        logger.info("FFMPEG thumbnail command: {}", strCommand);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        if (process.waitFor() != 0) {
            logger.error("FFMPEG process failed to generate thumbnail");
            throw new IOException("FFMPEG process failed to generate thumbnail");
        }

        logger.info("Thumbnail generated successfully at {}", outputFile.getAbsolutePath());
    }

    public void remux(File inputFile, File outputFile) throws IOException, InterruptedException {
        logger.info("Remuxing file {} to {}", inputFile.getName(), outputFile.getAbsolutePath());

        List<String> command = new ArrayList<>();
        command.add(FFMPEG_COMMAND);
        command.add("-fflags");
        command.add("+genpts");
        command.add("-i");
        command.add(inputFile.getAbsolutePath());
        command.add("-c:v");
        command.add("h264");
        command.add("-c:a");
        command.add("aac");
        command.add(outputFile.getAbsolutePath());

        String strCommand = String.join(" ", command);
        logger.info("FFMPEG remux command: {}", strCommand);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        if (process.waitFor() != 0) {
            logger.error("FFMPEG remux process failed");
            throw new IOException("FFMPEG remux process failed");
        }

        logger.info("Remuxing completed successfully");
    }

    private void updateJobProgress(Process process, ProgressTracker progress, Float length) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            logger.debug(line);
            Matcher matcher = timePattern.matcher(line);

            if (matcher.find()) {
                Float timeInMs = Float.parseFloat(matcher.group(1)) / 1000000f;
                progress.setProgress(timeInMs/length);
            }
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

    private List<String> buildCommand(File inputFile, File outputFile, VideoMetadata videoMetadata) {
        List<String> command = new ArrayList<>();
        command.add(FFMPEG_COMMAND);
        command.add("-progress");
        command.add("pipe:1");
        command.add("-y");

        Float length = videoMetadata.getEndPoint() - videoMetadata.getStartPoint();
        command.addAll(buildInputs(inputFile, videoMetadata.getStartPoint(), length));

        if (videoMetadata.getFps() != null || videoMetadata.getWidth() != null || videoMetadata.getHeight() != null) {
            command.addAll(buildFilters(videoMetadata.getFps(), videoMetadata.getWidth(), videoMetadata.getHeight()));
        }

        if (videoMetadata.getFileSize() != null) {
            command.addAll(buildBitrate(length, videoMetadata.getFileSize()));
        }

        // Output file
        command.add(outputFile.getAbsolutePath());
        return command;
    }
}
