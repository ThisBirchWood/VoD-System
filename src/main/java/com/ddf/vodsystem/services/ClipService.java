package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.ProgressTracker;
import com.ddf.vodsystem.dto.VideoMetadata;
import com.ddf.vodsystem.entities.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.ddf.vodsystem.exceptions.FFMPEGException;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.repositories.ClipRepository;
import com.ddf.vodsystem.services.media.CompressionService;
import com.ddf.vodsystem.services.media.MetadataService;
import com.ddf.vodsystem.services.media.ThumbnailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClipService {
    private static final Logger logger = LoggerFactory.getLogger(ClipService.class);

    private final ClipRepository clipRepository;
    private final DirectoryService directoryService;
    private final CompressionService compressionService;
    private final MetadataService metadataService;
    private final ThumbnailService thumbnailService;
    private final UserService userService;

    public ClipService(ClipRepository clipRepository,
                       DirectoryService directoryService,
                       CompressionService compressionService,
                       MetadataService metadataService,
                       ThumbnailService thumbnailService,
                       UserService userService) {
        this.clipRepository = clipRepository;
        this.directoryService = directoryService;
        this.compressionService = compressionService;
        this.metadataService = metadataService;
        this.thumbnailService = thumbnailService;
        this.userService = userService;
    }

    /**
     * Run the clip creation process.
     * This method normalizes the input metadata, compresses the video file,
     * updates the output metadata with the file size, and saves the clip
     * to the database if the user is authenticated.
     *
     * @param inputMetadata The metadata of the input video file.
     * @param outputMetadata The metadata for the output video file.
     * @param inputFile The input video file to be processed.
     * @param outputFile The output file where the processed video will be saved.
     * @param progress A tracker to monitor the progress of the video processing.
     * @throws IOException if an I/O error occurs during file processing.
     * @throws InterruptedException if the thread is interrupted during processing.
     */
    public void create(VideoMetadata inputMetadata,
                                 VideoMetadata outputMetadata,
                                 File inputFile,
                                 File outputFile,
                                 ProgressTracker progress)
            throws IOException, InterruptedException {

        User user = userService.getLoggedInUser();
        metadataService.normalizeVideoMetadata(inputMetadata, outputMetadata);
        compressionService.compress(inputFile, outputFile, outputMetadata, progress)
                .thenRun(() -> {
                    if (user != null) {
                        persistClip(outputMetadata, user, outputFile, inputFile.getName());
                    }
                });
    }

    public List<Clip> getClipsByUser() {
        User user = userService.getLoggedInUser();

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

    public boolean deleteClip(Long id) {
        Clip clip = getClipById(id);
        if (clip == null) {
            logger.warn("Clip with ID {} not found for deletion", id);
            return false;
        }

        if (!isAuthenticatedForClip(clip)) {
            logger.warn("User is not authorized to delete clip with ID {}", id);
            throw new NotAuthenticated("You are not authorized to delete this clip");
        }

        File clipFile = new File(clip.getVideoPath());
        File thumbnailFile = new File(clip.getThumbnailPath());
        directoryService.deleteFile(clipFile);
        directoryService.deleteFile(thumbnailFile);

        clipRepository.delete(clip);
        logger.info("Clip with ID {} deleted successfully", id);
        return true;
    }

    public boolean isAuthenticatedForClip(Clip clip) {
        User user = userService.getLoggedInUser();
        if (user == null || clip == null) {
            return false;
        }
        return user.getId().equals(clip.getUser().getId());
    }

    private void persistClip(VideoMetadata videoMetadata,
                               User user,
                               File tempFile,
                               String fileName) {
        // Move clip from temp to output directory
        File clipFile = directoryService.getUserClipsFile(user.getId(), fileName);
        File thumbnailFile = directoryService.getUserThumbnailsFile(user.getId(), fileName + ".png");
        directoryService.cutFile(tempFile, clipFile);

        VideoMetadata clipMetadata;
        try {
            clipMetadata = metadataService.getVideoMetadata(clipFile).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new FFMPEGException("Error retrieving video metadata for clip: " + e.getMessage());
        }


        try {
            thumbnailService.createThumbnail(clipFile, thumbnailFile, 0.0f);
        } catch (IOException | InterruptedException e) {
            logger.error("Error generating thumbnail for clip: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

        // Save clip to database
        Clip clip = new Clip();
        clip.setUser(user);
        clip.setTitle(videoMetadata.getTitle() != null ? videoMetadata.getTitle() : "Untitled Clip");
        clip.setDescription(videoMetadata.getDescription() != null ? videoMetadata.getDescription() : "");
        clip.setCreatedAt(LocalDateTime.now());
        clip.setWidth(clipMetadata.getWidth());
        clip.setHeight(clipMetadata.getHeight());
        clip.setFps(clipMetadata.getFps());
        clip.setDuration(clipMetadata.getEndPoint() - clipMetadata.getStartPoint());
        clip.setFileSize(clipMetadata.getFileSize());
        clip.setVideoPath(clipFile.getPath());
        clip.setThumbnailPath(thumbnailFile.getPath());
        clipRepository.save(clip);

        logger.info("Clip created successfully with ID: {}", clip.getId());
    }
}
