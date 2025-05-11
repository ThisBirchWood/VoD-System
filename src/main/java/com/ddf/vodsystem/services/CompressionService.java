package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.ClipConfig;
import com.ddf.vodsystem.entities.JobStatus;
import com.ddf.vodsystem.entities.Job;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CompressionService {
    private static final Logger logger = LoggerFactory.getLogger(CompressionService.class);

    private static final float AUDIO_RATIO = 0.15f;
    private static final float MAX_AUDIO_BITRATE = 128f;
    private static final float BITRATE_MULTIPLIER = 0.9f;

    private Pattern timePattern = Pattern.compile("out_time_ms=([\\d:.]+)");
    private long out_time_ms;

    private void buildFilters(ArrayList<String> command, Float fps, Integer width, Integer height) {
        List<String> filters = new ArrayList<>();

        if (fps != null) {
            filters.add("fps=" + fps);
        }

        if (!(width == null && height == null)) {
            String w = (width != null) ? width.toString() : "-1";
            String h = (height != null) ? height.toString() : "-1";
            filters.add("scale=" + w + ":" + h);
        }

        if (!filters.isEmpty()) {
            command.add("-vf");
            command.add(String.join(",", filters));
        }
    }

    private void buildBitrate(ArrayList<String> command, Float length, Float fileSize) {
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

    private void buildInputs(ArrayList<String> command, File inputFile, Float startPoint, Float endPoint) {
        if (startPoint == null) {
            startPoint = 0f;
        }

        command.add("-ss");
        command.add(startPoint.toString());

        command.add("-i");
        command.add(inputFile.getAbsolutePath());

        if (endPoint != null) {
            Float length = endPoint - startPoint;
            command.add("-t");
            command.add(length.toString());
        }
    }

    private ProcessBuilder buildCommand(File inputFile, File outputFile, ClipConfig clipConfig) {
        ArrayList<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-progress");
        command.add("pipe:1");
        command.add("-y");

        Float length = clipConfig.getEndPoint() - clipConfig.getStartPoint();
        buildInputs(command, inputFile, clipConfig.getStartPoint(), clipConfig.getEndPoint());
        buildFilters(command, clipConfig.getFps(), clipConfig.getWidth(), clipConfig.getHeight());

        if (clipConfig.getFileSize() != null) {
            buildBitrate(command, length, clipConfig.getFileSize());
        }

        // Output file
        command.add(outputFile.getAbsolutePath());

        logger.info("Running command: {}", String.join(" ", command));
        return new ProcessBuilder(command);
    }

    public void run(Job job) throws IOException, InterruptedException {
        logger.info("FFMPEG starting...");

        ProcessBuilder pb = buildCommand(job.getInputFile(), job.getOutputFile(), job.getClipConfig());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        job.setStatus(JobStatus.RUNNING);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        Float length = job.getClipConfig().getEndPoint() - job.getClipConfig().getStartPoint();

        String line;
        while ((line = reader.readLine()) != null) {
            logger.debug(line);
            Matcher matcher = timePattern.matcher(line);
            if (matcher.find()) {
                Float progress = Float.parseFloat(matcher.group(1))/(length*1000000);
                job.setProgress(progress);
            }
        }

        job.setStatus(JobStatus.FINISHED);
        logger.info("FFMPEG finished");
    }

}
