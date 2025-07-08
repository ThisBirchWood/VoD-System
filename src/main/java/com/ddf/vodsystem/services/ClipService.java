package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
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

    private final ClipRepository clipRepository;
    private final MetadataService metadataService;
    private final DirectoryService directoryService;
    private final FfmpegService ffmpegService;
    private final Pattern timePattern = Pattern.compile("out_time_ms=(\\d+)");

    public ClipService(ClipRepository clipRepository,
                       MetadataService metadataService,
                       DirectoryService directoryService,
                       FfmpegService ffmpegService) {
        this.clipRepository = clipRepository;
        this.metadataService = metadataService;
        this.directoryService = directoryService;
        this.ffmpegService = ffmpegService;
    }

    /**
     * Runs the FFMPEG command to create a video clip based on the provided job.
     * Updates the job status and progress as the command executes.
     * This method validates the input and output video metadata,
     * Updates the job VideoMetadata with the output file size,
     *
     * @param job the job containing input and output video metadata
     * @throws IOException if an I/O error occurs during command execution
     * @throws InterruptedException if the thread is interrupted while waiting for the process to finish
     *
     */
    public void run(Job job) throws IOException, InterruptedException {
        logger.info("FFMPEG starting...");
        metadataService.normalizeVideoMetadata(job.getInputVideoMetadata(), job.getOutputVideoMetadata());

        ProcessBuilder pb = ffmpegService.buildCommand(job.getInputFile(), job.getOutputFile(), job.getOutputVideoMetadata());
        Process process = pb.start();
        job.setStatus(JobStatus.RUNNING);

        updateJobProgress(process, job);

        if (process.waitFor() != 0) {
            job.setStatus(JobStatus.FAILED);
            throw new FFMPEGException("FFMPEG process failed");
        }

        Float fileSize = metadataService.getFileSize(job.getOutputFile());
        job.getOutputVideoMetadata().setFileSize(fileSize);

        User user = getUser();
        if (user != null) {
            persistClip(job.getOutputVideoMetadata(), user, job);
        }

        job.setStatus(JobStatus.FINISHED);
        logger.info("FFMPEG finished");
    }

    private void updateJobProgress(Process process, Job job) throws IOException {
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
    }

    private User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomOAuth2User oAuth2user) {
            return oAuth2user.getUser();
        }
        return null;
    }

    private void persistClip(VideoMetadata videoMetadata, User user, Job job) {
        // Move clip from temp to output directory
        String fileExtension = directoryService.getFileExtension(job.getOutputFile().getAbsolutePath());
        File outputFile = directoryService.getOutputFile(job.getUuid(), fileExtension);
        directoryService.copyFile(job.getOutputFile(), outputFile);

        // Save clip to database
        Clip clip = new Clip();
        clip.setUser(user);
        clip.setTitle(videoMetadata.getTitle() != null ? videoMetadata.getTitle() : "Untitled Clip");
        clip.setDescription(videoMetadata.getDescription());
        clip.setCreatedAt(LocalDateTime.now());
        clip.setWidth(videoMetadata.getWidth());
        clip.setHeight(videoMetadata.getHeight());
        clip.setFps(videoMetadata.getFps());
        clip.setDuration(videoMetadata.getEndPoint() - videoMetadata.getStartPoint());
        clip.setFileSize(videoMetadata.getFileSize());
        clip.setVideoPath(outputFile.getPath());
        clipRepository.save(clip);
    }
}
