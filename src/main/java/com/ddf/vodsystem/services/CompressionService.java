package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.ClipConfig;
import lombok.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Long.parseLong;

public class CompressionService {
    private static final Logger logger = LoggerFactory.getLogger(CompressionService.class);

    private List<String> command;

    @Getter @Setter
    private File inputFile;
    @Getter @Setter
    private File outputFile;
    @Getter @Setter
    private ClipConfig clipConfig;

    private final Float startPoint;
    private final Float endPoint;
    private final Integer width;
    private final Integer height;
    private final Float fps;
    private final Float fileSize;

    private static final float AUDIO_RATIO = 0.15f;
    private static final float MAX_AUDIO_BITRATE = 128f;
    private static final float BITRATE_MULTIPLIER = 0.9f;

    private Pattern timePattern = Pattern.compile("time=([\\d:.]+)");
    private long out_time_ms;

    public CompressionService(File file, File output, ClipConfig clipConfig) {
        command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-progress");
        command.add("pipe:1");
        command.add("-y");

        this.inputFile = file;
        this.outputFile = output;
        this.clipConfig = clipConfig;
        this.startPoint = clipConfig.getStartPoint();
        this.endPoint = clipConfig.getEndPoint();
        this.fps = clipConfig.getFps();
        this.fileSize = clipConfig.getFileSize();
        this.width = clipConfig.getWidth();
        this.height = clipConfig.getHeight();

    }

    private void buildFilters() {
        List<String> filters = new ArrayList<>();

        if (fps != null) {
            filters.add("fps=" + fps);
        }

        if ((width != null && height == null) || (height != null && width == null)) {
            String w = (width != null) ? width.toString() : "-1";
            String h = (height != null) ? height.toString() : "-1";
            filters.add("scale=" + w + ":" + h);
        }

        if (!filters.isEmpty()) {
            command.add("-vf");
            command.add(String.join(",", filters));
        }
    }

    private void buildBitrate() {
        float length = endPoint - startPoint;
        float bitrate = ((fileSize * 8) / length) * BITRATE_MULTIPLIER;

        float audio_bitrate = bitrate * AUDIO_RATIO;
        float video_bitrate;

        if (audio_bitrate > MAX_AUDIO_BITRATE) {
            audio_bitrate = MAX_AUDIO_BITRATE;
            video_bitrate = bitrate - MAX_AUDIO_BITRATE;
        } else {
            video_bitrate = bitrate * (1 - AUDIO_RATIO);
        }

        command.add("-b:v");
        command.add(video_bitrate + "k");
        command.add("-b:a");
        command.add(audio_bitrate + "k");
    }

    private void buildInputs(){
        if (startPoint != null) {
            command.add("-ss");
            command.add(startPoint.toString());
        }

        command.add("-i");
        command.add(inputFile.getAbsolutePath());

        if (endPoint != null) {
            command.add("-t");

            Float duration = endPoint - startPoint;
            command.add(duration.toString());
        }
    }

    private ProcessBuilder buildCommand() {
        buildInputs();
        buildFilters();

        if (fileSize != null) {
            buildBitrate();
        }

        // Output file
        command.add(outputFile.getAbsolutePath());

        logger.info("Running command: {}", String.join(" ", command));
        return new ProcessBuilder(command);
    }

    public void run() throws IOException, InterruptedException {
        logger.info("FFMPEG starting...");
        ProcessBuilder pb = buildCommand();
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            logger.debug(line);
//            if (line.startsWith("out_time_ms=")) {
//                out_time_ms = parseLong(line.substring("out_time_ms=".length()));
//            }
        }

        logger.info("FFMPEG finished");
    }

}
