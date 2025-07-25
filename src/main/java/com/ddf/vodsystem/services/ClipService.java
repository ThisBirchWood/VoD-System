package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.ProgressTracker;
import com.ddf.vodsystem.dto.VideoMetadata;
import com.ddf.vodsystem.entities.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
     * @return An Optional containing the created Clip if the user is authenticated, otherwise an empty Optional.
     */
    public Optional<Clip> create(VideoMetadata inputMetadata,
                                 VideoMetadata outputMetadata,
                                 File inputFile,
                                 File outputFile,
                                 ProgressTracker progress) throws IOException, InterruptedException {
        metadataService.normalizeVideoMetadata(inputMetadata, outputMetadata);
        mediaService.compress(inputFile, outputFile, outputMetadata, progress);

        Float fileSize = metadataService.getFileSize(outputFile);
        outputMetadata.setFileSize(fileSize);

        User user = userService.getUser();
        if (user != null) {
            return Optional.of(persistClip(outputMetadata, user, outputFile, inputFile.getName()));
        }

        return Optional.empty();
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

    public void deleteClip(Long id) {
        Clip clip = getClipById(id);
        if (clip == null) {
            logger.warn("Clip with ID {} not found for deletion", id);
            return;
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
    }

    public boolean isAuthenticatedForClip(Clip clip) {
        User user = userService.getUser();
        if (user == null || clip == null) {
            return false;
        }
        return user.getId().equals(clip.getUser().getId());
    }

    private Clip persistClip(VideoMetadata videoMetadata,
                             User user,
                             File tempFile,
                             String fileName) {
        // Move clip from temp to output directory
        File clipFile = directoryService.getUserClipsFile(user.getId(), fileName);
        File thumbnailFile = directoryService.getUserThumbnailsFile(user.getId(), fileName + ".png");
        directoryService.cutFile(tempFile, clipFile);


        try {
            mediaService.createThumbnail(clipFile, thumbnailFile, 0.0f);
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
        clip.setVideoPath(clipFile.getPath());
        clip.setThumbnailPath(thumbnailFile.getPath());
        return clipRepository.save(clip);
    }
}
