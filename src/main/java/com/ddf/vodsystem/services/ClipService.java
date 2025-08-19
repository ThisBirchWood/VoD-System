package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.ProgressTracker;
import com.ddf.vodsystem.dto.options.ClipOptions;
import com.ddf.vodsystem.entities.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    public void create(ClipOptions inputMetadata,
                       ClipOptions outputMetadata,
                       File inputFile,
                       File outputFile,
                       ProgressTracker progress)
            throws IOException, InterruptedException {

        Optional<User> user = userService.getLoggedInUser();
        metadataService.normalizeVideoMetadata(inputMetadata, outputMetadata);
        compressionService.compress(inputFile, outputFile, outputMetadata, progress)
                .thenRun(() -> user.ifPresent(value ->
                        persistClip(
                                outputMetadata,
                                value,
                                outputFile,
                                inputFile.getName()
                        )));
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

    private void persistClip(ClipOptions clipOptions,
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
            logger.error("Error generating thumbnail for clip: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

        // Save clip to database
        Clip clip = new Clip();
        clip.setUser(user);
        clip.setTitle(clipOptions.getTitle() != null ? clipOptions.getTitle() : "Untitled Clip");
        clip.setDescription(clipOptions.getDescription() != null ? clipOptions.getDescription() : "");
        clip.setCreatedAt(LocalDateTime.now());
        clip.setWidth(clipMetadata.getWidth());
        clip.setHeight(clipMetadata.getHeight());
        clip.setFps(clipMetadata.getFps());
        clip.setDuration(clipMetadata.getDuration() - clipMetadata.getStartPoint());
        clip.setFileSize(clipMetadata.getFileSize());
        clip.setVideoPath(clipFile.getPath());
        clip.setThumbnailPath(thumbnailFile.getPath());
        clipRepository.save(clip);

        logger.info("Clip created successfully with ID: {}", clip.getId());
    }

    private void deleteClipFiles(Clip clip) {
        File clipFile = new File(clip.getVideoPath());
        File thumbnailFile = new File(clip.getThumbnailPath());

        boolean clipDeleted = directoryService.deleteFile(clipFile);
        boolean thumbnailDeleted = directoryService.deleteFile(thumbnailFile);

        if (!clipDeleted) {
            throw new FFMPEGException("Failed to delete clip file: " + clipFile.getAbsolutePath());
        }

        if (!thumbnailDeleted) {
            throw new FFMPEGException("Failed to delete thumbnail file: " + thumbnailFile.getAbsolutePath());
        }
    }
}
