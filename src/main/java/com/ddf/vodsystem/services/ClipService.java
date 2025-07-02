package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ddf.vodsystem.exceptions.FFMPEGException;
import com.ddf.vodsystem.repositories.ClipRepository;
import com.ddf.vodsystem.security.CustomOAuth2User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ClipService {
    private static final Logger logger = LoggerFactory.getLogger(ClipService.class);

    private static final float AUDIO_RATIO = 0.15f;
    private static final float MAX_AUDIO_BITRATE = 128f;
    private static final float BITRATE_MULTIPLIER = 0.9f;

    private final ClipRepository clipRepository;
    private final Pattern timePattern = Pattern.compile("out_time_ms=(\\d+)");

    public ClipService(ClipRepository clipRepository) {
        this.clipRepository = clipRepository;
    }

    public void run(Job job) throws IOException, InterruptedException {
        logger.info("FFMPEG starting...");

        validateVideoMetadata(job.getInputVideoMetadata(), job.getOutputVideoMetadata());

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

        User user = getUser();
        if (user != null) {
            createClip(job.getOutputVideoMetadata(), user);
        }

        job.setStatus(JobStatus.FINISHED);
        logger.info("FFMPEG finished");
    }

    private User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomOAuth2User oAuth2user) {
            return oAuth2user.getUser();
        }
        return null;
    }

    private void validateVideoMetadata(VideoMetadata inputFileMetadata, VideoMetadata outputFileMetadata) {
        if (outputFileMetadata.getStartPoint() == null) {
            outputFileMetadata.setStartPoint(0f);
        }

        if (outputFileMetadata.getEndPoint() == null) {
            outputFileMetadata.setEndPoint(inputFileMetadata.getEndPoint());
        }
    }

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

    private void buildInputs(ArrayList<String> command, File inputFile, Float startPoint, Float length) {
        command.add("-ss");
        command.add(startPoint.toString());

        command.add("-i");
        command.add(inputFile.getAbsolutePath());

        command.add("-t");
        command.add(Float.toString(length));
    }

    private ProcessBuilder buildCommand(File inputFile, File outputFile, VideoMetadata videoMetadata) {
        ArrayList<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-progress");
        command.add("pipe:1");
        command.add("-y");

        Float length = videoMetadata.getEndPoint() - videoMetadata.getStartPoint();
        buildInputs(command, inputFile, videoMetadata.getStartPoint(), length);
        buildFilters(command, videoMetadata.getFps(), videoMetadata.getWidth(), videoMetadata.getHeight());

        if (videoMetadata.getFileSize() != null) {
            buildBitrate(command, length, videoMetadata.getFileSize());
        }

        // Output file
        command.add(outputFile.getAbsolutePath());

        logger.info("Running command: {}", command);
        return new ProcessBuilder(command);
    }

    private void createClip(VideoMetadata videoMetadata, User user) {
        Clip clip = new Clip();
        clip.setTitle(videoMetadata.getTitle() != null ? videoMetadata.getTitle() : "Untitled Clip");
        clip.setUser(user);
        clip.setDescription(videoMetadata.getDescription());
        clip.setCreatedAt(LocalDateTime.now());
        clip.setWidth(videoMetadata.getWidth());
        clip.setHeight(videoMetadata.getHeight());
        clip.setFps(videoMetadata.getFps());
        clip.setDuration(videoMetadata.getEndPoint() - videoMetadata.getStartPoint());
        clip.setFileSize(videoMetadata.getFileSize());
        clip.setVideoPath("test");
        clipRepository.save(clip);
    }

}
