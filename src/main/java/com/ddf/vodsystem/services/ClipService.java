package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.ClipOptions;
import com.ddf.vodsystem.entities.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.ddf.vodsystem.exceptions.FFMPEGException;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.repositories.ClipRepository;
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
    private final MetadataService metadataService;
    private final ThumbnailService thumbnailService;
    private final UserService userService;

    public ClipService(ClipRepository clipRepository,
                       DirectoryService directoryService,
                       MetadataService metadataService,
                       ThumbnailService thumbnailService,
                       UserService userService) {
        this.clipRepository = clipRepository;
        this.directoryService = directoryService;
        this.metadataService = metadataService;
        this.thumbnailService = thumbnailService;
        this.userService = userService;
    }

    /**
     * Retrieves all clips associated with the currently logged-in user.
     *
     * @return a list of clips belonging to the authenticated user.
     * @throws NotAuthenticated if the user is not authenticated.
     */
    public List<Clip> getClipsByUser() {
        Optional<User> user = userService.getLoggedInUser();

        if (user.isEmpty()) {
            throw new NotAuthenticated("User is not authenticated");
        }

        return clipRepository.findByUser(user.get());
    }

    /**
     * Retrieves a clip by its ID, ensuring the user is authenticated to access it.
     *
     * @param id the ID of the clip to retrieve.
     * @return an Optional containing the Clip if found and accessible, or empty if not found.
     * @throws NotAuthenticated if the user is not authorized to access the clip.
     */
    public Optional<Clip> getClipById(Long id) {
        Optional<Clip> clip = clipRepository.findById(id);

        if (clip.isEmpty()) {
            logger.warn("Clip with ID {} not found", id);
            return clip;
        }

        if (!isAuthenticatedForClip(clip.get())) {
            logger.warn("User is not authorized to access clip with ID {}", id);
            throw new NotAuthenticated("You are not authorized to access this clip");
        }

        return clip;
    }

    /**
     * Deletes a clip by its ID, ensuring the user is authenticated to perform the deletion.
     *
     * @param id the ID of the clip to delete.
     * @return true if the clip was successfully deleted, false if it was not found.
     * @throws NotAuthenticated if the user is not authorized to delete the clip.
     */
    public boolean deleteClip(Long id) {
        Optional<Clip> possibleClip = getClipById(id);
        if (possibleClip.isEmpty()) {
            logger.warn("Clip with ID {} not found for deletion", id);
            return false;
        }

        Clip clip = possibleClip.get();
        if (!isAuthenticatedForClip(clip)) {
            throw new NotAuthenticated("You are not authorized to delete this clip");
        }

        deleteClipFiles(clip);
        clipRepository.delete(clip);

        logger.info("Clip with ID {} deleted successfully", id);
        return true;
    }

    /**
     * Checks if the currently logged-in user is authenticated to access the specified clip.
     *
     * @param clip the clip to check access for.
     * @return true if the user is authenticated for the clip, false otherwise.
     */
    public boolean isAuthenticatedForClip(Clip clip) {
        Optional<User> user = userService.getLoggedInUser();
        if (user.isEmpty() || clip == null) {
            return false;
        }
        return user.get().getId().equals(clip.getUser().getId());
    }

    /**
     * Persists a clip to the database
     * @param options ClipOptions object of the clip metadata to save to the database. All fields required except for title, description
     * @param user User to save the clip to
     * @param videoPath Path of the clip
     * @param thumbnailPath Path of the thumbnail
     * @return Clip object saved to the database
     */
    public Clip saveClip(ClipOptions options,
                         User user,
                         String videoPath,
                         String thumbnailPath) {
        Clip clip = new Clip();
        clip.setUser(user);
        clip.setTitle(options.getTitle() != null ? options.getTitle() : "Untitled Clip");
        clip.setDescription(options.getDescription() != null ? options.getDescription() : "");
        clip.setCreatedAt(LocalDateTime.now());
        clip.setWidth(options.getWidth());
        clip.setHeight(options.getHeight());
        clip.setFps(options.getFps());
        clip.setDuration(options.getDuration() - options.getStartPoint());
        clip.setFileSize(options.getFileSize());
        clip.setVideoPath(videoPath);
        clip.setThumbnailPath(thumbnailPath);
        return clipRepository.save(clip);
    }

    public void persistClip(ClipOptions clipOptions,
                             User user,
                             File tempFile,
                             String fileName) {
        // Move clip from temp to output directory
        File clipFile = directoryService.getUserClipsFile(user.getId(), fileName);
        File thumbnailFile = directoryService.getUserThumbnailsFile(user.getId(), fileName + ".png");
        directoryService.cutFile(tempFile, clipFile);

        ClipOptions clipMetadata;
        try {
            clipMetadata = metadataService.getVideoMetadata(clipFile).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new FFMPEGException("Error retrieving video metadata for clip: " + e.getMessage());
        }

        try {
            thumbnailService.createThumbnail(clipFile, thumbnailFile, 0.0f);
        } catch (IOException | InterruptedException e) {
            logger.error("Error generating thumbnail for user: {}, {}", user.getId(), e.getMessage());
            Thread.currentThread().interrupt();
        }

        clipMetadata.setTitle(clipOptions.getTitle());
        clipMetadata.setDescription(clipOptions.getDescription());

        Clip clip = saveClip(clipMetadata, user, clipFile.getAbsolutePath(), thumbnailFile.getAbsolutePath());
        logger.info("Clip created successfully with ID: {}", clip.getId());
    }

    private void deleteClipFiles(Clip clip) {
        File clipFile = new File(clip.getVideoPath());
        File thumbnailFile = new File(clip.getThumbnailPath());

        try {
            Files.deleteIfExists(clipFile.toPath());
            Files.deleteIfExists(thumbnailFile.toPath());
        } catch (IOException e) {
            logger.error("Could not delete clip files for clip ID: {}", clip.getId());
        }
    }
}
