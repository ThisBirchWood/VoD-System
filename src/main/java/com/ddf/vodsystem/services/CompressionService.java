package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.VideoMetadata;
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

import com.ddf.vodsystem.exceptions.FFMPEGException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CompressionService {
    private static final Logger logger = LoggerFactory.getLogger(CompressionService.class);

    private static final float AUDIO_RATIO = 0.15f;
    private static final float MAX_AUDIO_BITRATE = 128f;
    private static final float BITRATE_MULTIPLIER = 0.9f;

    private final Pattern timePattern = Pattern.compile("out_time_ms=(\\d+)");

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
            float length = endPoint - startPoint;
            command.add("-t");
            command.add(Float.toString(length));
        }
    }

    private ProcessBuilder buildCommand(File inputFile, File outputFile, VideoMetadata videoMetadata) {
        ArrayList<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-progress");
        command.add("pipe:1");
        command.add("-y");

        Float length = videoMetadata.getEndPoint() - videoMetadata.getStartPoint();
        buildInputs(command, inputFile, videoMetadata.getStartPoint(), videoMetadata.getEndPoint());
        buildFilters(command, videoMetadata.getFps(), videoMetadata.getWidth(), videoMetadata.getHeight());

        if (videoMetadata.getFileSize() != null) {
            buildBitrate(command, length, videoMetadata.getFileSize());
        }

        // Output file
        command.add(outputFile.getAbsolutePath());

        logger.info("Running command: {}", command);
        return new ProcessBuilder(command);
    }

    public void run(Job job) throws IOException, InterruptedException {
        logger.info("FFMPEG starting...");

        ProcessBuilder pb = buildCommand(job.getInputFile(), job.getOutputFile(), job.getOutputVideoMetadata());
        Process process = pb.start();
        job.setStatus(JobStatus.RUNNING);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        float length = job.getOutputVideoMetadata().getEndPoint() - job.getOutputVideoMetadata().getStartPoint();

        String line;
        while ((line = reader.readLine()) != null) {
            logger.debug(line);
            Matcher matcher = timePattern.matcher(line);

            if (matcher.find()) {
                Float progress = Long.parseLong(matcher.group(1))/(length*1000000);
                job.setProgress(progress);
            }
        }

        if (process.waitFor() != 0) {
            job.setStatus(JobStatus.FAILED);
            throw new FFMPEGException("FFMPEG process failed");
        }

        job.setStatus(JobStatus.FINISHED);
        logger.info("FFMPEG finished");
    }

}
