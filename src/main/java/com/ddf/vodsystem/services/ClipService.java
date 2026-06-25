package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.ClipOptions;
import com.ddf.vodsystem.controllers.dto.ClipUpdateRequest;
import com.ddf.vodsystem.entities.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.ddf.vodsystem.exceptions.*;
import com.ddf.vodsystem.repositories.ClipRepository;
import com.ddf.vodsystem.services.media.MetadataService;
import com.ddf.vodsystem.services.media.ThumbnailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
            throw new ClipNotFound("Clip with id " + id + " not found.");
        }

        if (!isAuthenticatedForClip(clip.get())) {
            throw new NotAuthenticated("You are not authorized to access clip: " + id);
        }

        return clip;
    }

    public Clip updateClip(Long id, ClipUpdateRequest newFields) {
        Optional<Clip> possibleClip = clipRepository.findById(id);

        if (possibleClip.isEmpty()) {
            throw new ClipNotFound("Clip with id " + id + " not found.");
        }

        Clip clip = possibleClip.get();

        if (!isAuthenticatedForClip(clip)) {
            throw new NotAuthenticated("You are not authorized to access clip: " + id);
        }

        if (newFields.title() != null) {
            clip.setTitle(newFields.title());
        }

        if (newFields.description() != null) {
            clip.setDescription(newFields.description());
        }

        return clipRepository.saveAndFlush(clip);
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

    public Resource downloadClip(Long id) {
        Optional<Clip> possibleClip = getClipById(id);

        if (possibleClip.isEmpty()) {
            throw new ClipNotFound("Clip " + id + " doesn't exist");
        }

        Clip clip = possibleClip.get();

        if (!isAuthenticatedForClip(clip)) {
            throw new NotAuthenticated("Not authenticated for this clip");
        }

        String path = clip.getVideoPath();
        Path file = directoryService.resolvePath(path);

        if (!Files.exists(file)) {
            throw new JobNotFound("Clip file not found");
        }

        return new FileSystemResource(file);
    }

    public Resource downloadThumbnail(Long id) {
        Optional<Clip> possibleClip = getClipById(id);

        if (possibleClip.isEmpty()) {
            throw new ClipNotFound("Clip " + id + " doesn't exist");
        }

        Clip clip = possibleClip.get();

        if (!isAuthenticatedForClip(clip)) {
            throw new NotAuthenticated("Not authenticated for this clip thumbnail");
        }

        String path = clip.getThumbnailPath();
        Path file = directoryService.resolvePath(path);

        if (!Files.exists(file)) {
            throw new JobNotFound("Thumbnail file not found");
        }

        return new FileSystemResource(file);
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
                         Path videoPath,
                         Path thumbnailPath) {
        Clip clip = new Clip();
        clip.setUser(user);
        clip.setTitle(options.getTitle() != null ? options.getTitle() : "Untitled Clip");
        clip.setDescription(options.getDescription() != null ? options.getDescription() : "");
        clip.setCreatedAt(Instant.now());
        clip.setWidth(options.getWidth());
        clip.setHeight(options.getHeight());
        clip.setFps(options.getFps());
        clip.setDuration(options.getDuration() - options.getStartPoint());
        clip.setFileSize(options.getFileSize());
        clip.setVideoPath(directoryService.relativisePath(videoPath.toAbsolutePath()).toString());
        clip.setThumbnailPath(directoryService.relativisePath(thumbnailPath.toAbsolutePath()).toString());
        return clipRepository.save(clip);
    }

    public void persistClip(String title,
                            String description,
                            User user,
                            Path clipFile,
                            String fileName) {
        Path newClipFile;
        Path thumbnailFile;

        // Move temp file from temp dir to output dir
        try {
            newClipFile = directoryService.getClipsDir(user.getId()).resolve(fileName);
            thumbnailFile = directoryService.getThumbnailsDir(user.getId()).resolve(fileName + ".png");
            directoryService.copyFile(clipFile, newClipFile);
        } catch (IOException e) {
            throw new StorageException("Failed to move clip from temporary directory to output directory", e);
        }

        ClipOptions clipMetadata;
        try {
            clipMetadata = metadataService.getVideoMetadata(newClipFile).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new FFMPEGException("Error retrieving video metadata for clip: " + e.getMessage());
        }

        // Thumbnail generation can fail with error propagation
        try {
            thumbnailService.createThumbnail(newClipFile, thumbnailFile, 0.0f);
        } catch (InterruptedException e) {
            logger.error("Thumbnail generation interrupted for user: {}", user.getId(), e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.error("Error generating thumbnail for user: {}", user.getId(), e);
        }

        clipMetadata.setTitle(title);
        clipMetadata.setDescription(description);

        Clip clip = saveClip(clipMetadata, user, newClipFile, thumbnailFile);
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
