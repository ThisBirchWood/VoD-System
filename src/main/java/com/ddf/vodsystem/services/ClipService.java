package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.VideoMetadata;
import com.ddf.vodsystem.entities.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.repositories.ClipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClipService {
    private static final Logger logger = LoggerFactory.getLogger(ClipService.class);

    private final ClipRepository clipRepository;
    private final MetadataService metadataService;
    private final DirectoryService directoryService;
    private final MediaService mediaService;
    private final UserService userService;

    public ClipService(ClipRepository clipRepository,
                       MetadataService metadataService,
                       DirectoryService directoryService,
                       MediaService mediaService,
                       UserService userService) {
        this.clipRepository = clipRepository;
        this.metadataService = metadataService;
        this.directoryService = directoryService;
        this.mediaService = mediaService;
        this.userService = userService;
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
        metadataService.normalizeVideoMetadata(job.getInputVideoMetadata(), job.getOutputVideoMetadata());
        mediaService.compress(job.getInputFile(), job.getOutputFile(), job.getOutputVideoMetadata(), job.getProgress());

        Float fileSize = metadataService.getFileSize(job.getOutputFile());
        job.getOutputVideoMetadata().setFileSize(fileSize);

        User user = userService.getUser();
        if (user != null) {
            persistClip(job.getOutputVideoMetadata(), user, job);
        }

        job.setStatus(JobStatus.FINISHED);
        logger.info("FFMPEG finished successfully for job: {}", job.getUuid());
    }

    public List<Clip> getClipsByUser() {
        User user = userService.getUser();

        if (user == null) {
            logger.warn("No authenticated user found");
            return List.of();
        }

        return clipRepository.findByUser(user);
    }

    public Clip getClipById(Long id) {
        Clip clip = clipRepository.findById(id).orElse(null);

        if (clip == null) {
            logger.warn("Clip with ID {} not found", id);
            return null;
        }

        if (!isAuthenticatedForClip(clip)) {
            logger.warn("User is not authorized to access clip with ID {}", id);
            throw new NotAuthenticated("You are not authorized to access this clip");
        }

        return clip;
    }

    public boolean isAuthenticatedForClip(Clip clip) {
        User user = userService.getUser();
        if (user == null || clip == null) {
            return false;
        }
        return user.getId().equals(clip.getUser().getId());
    }

    private void persistClip(VideoMetadata videoMetadata, User user, Job job) {
        // Move clip from temp to output directory
        String fileExtension = directoryService.getFileExtension(job.getOutputFile().getAbsolutePath());

        File clipOutputDir = directoryService.getUserClipsDir(user.getId());
        File clipOutputFile = new File(clipOutputDir, job.getUuid() + "." + fileExtension);
        directoryService.copyFile(job.getOutputFile(), clipOutputFile);

        File thumbnailOutputDir = directoryService.getUserThumbnailsDir(user.getId());
        File thumbnailOutputFile = new File(thumbnailOutputDir, job.getUuid() + ".png");

        try {
            mediaService.createThumbnail(clipOutputFile, thumbnailOutputFile, 0.0f);
        } catch (IOException | InterruptedException e) {
            logger.error("Error generating thumbnail for clip: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

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
        clip.setVideoPath(clipOutputFile.getPath());
        clip.setThumbnailPath(thumbnailOutputFile.getPath());
        clipRepository.save(clip);
    }
}
