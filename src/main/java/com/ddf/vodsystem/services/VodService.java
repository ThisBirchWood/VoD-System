package com.ddf.vodsystem.services;

import com.ddf.vodsystem.controllers.dto.VodUpdateRequest;
import com.ddf.vodsystem.dto.ClipOptions;
import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.entities.Vod;
import com.ddf.vodsystem.exceptions.*;
import com.ddf.vodsystem.repositories.VodRepository;
import com.ddf.vodsystem.services.media.MetadataService;
import com.ddf.vodsystem.services.media.ThumbnailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class VodService {
    private static final Logger logger = LoggerFactory.getLogger(VodService.class);

    private final VodRepository vodRepository;
    private final DirectoryService directoryService;
    private final MetadataService metadataService;
    private final ThumbnailService thumbnailService;
    private final UserService userService;

    public VodService(VodRepository vodRepository,
                      DirectoryService directoryService,
                      MetadataService metadataService,
                      ThumbnailService thumbnailService,
                      UserService userService) {
        this.vodRepository = vodRepository;
        this.directoryService = directoryService;
        this.metadataService = metadataService;
        this.thumbnailService = thumbnailService;
        this.userService = userService;
    }

    public Vod getVodById(Long id) {
        Optional<Vod> vod = vodRepository.findById(id);

        if (vod.isEmpty()) {
            throw new VodNotFound("Vod ID " + id + " does not exist");
        }

        if (!isAuthenticatedForVod(vod.get())) {
            throw new NotAuthenticated("User not authenticated");
        }

        return vod.get();
    }

    public List<Vod> getUserVods() {
        Optional<User> user = userService.getLoggedInUser();

        if (user.isEmpty()) {
            throw new NotAuthenticated("User must be logged in to retrieve vods");
        }

        return vodRepository.findByUser(user.get());
    }

    public Vod updateVod(Long id, VodUpdateRequest newFields) {
        Vod vod = getVodById(id);

        if (newFields.title() != null) {
            vod.setTitle(newFields.title());
        }

        if (newFields.description() != null) {
            vod.setDescription(newFields.description());
        }

        return vodRepository.saveAndFlush(vod);
    }

    public boolean deleteVod(Long id) {
        Vod vod = getVodById(id);

        deleteVodFiles(vod);
        vodRepository.delete(vod);

        logger.info("Vod with ID {} deleted successfully", id);
        return true;
    }

    public Resource downloadVod(Long id) {
        Vod vod = getVodById(id);
        Path file = directoryService.resolvePath(vod.getVideoPath());

        if (!Files.exists(file)) {
            throw new VodNotFound("Vod file not found for ID: " + id);
        }

        return new FileSystemResource(file);
    }

    public Resource downloadThumbnail(Long id) {
        Vod vod = getVodById(id);
        Path file = directoryService.resolvePath(vod.getThumbnailPath());

        if (!Files.exists(file)) {
            throw new VodNotFound("Thumbnail file not found for vod ID: " + id);
        }

        return new FileSystemResource(file);
    }

    public void persist(String title,
                        String description,
                        User user,
                        Path vodFile,
                        String fileName) {
        Path newVodFile;
        Path thumbnailFile;

        try {
            newVodFile = directoryService.getVodsDir(user.getId()).resolve(fileName);
            thumbnailFile = directoryService.getThumbnailsDir(user.getId()).resolve(fileName + ".png");
            directoryService.copyFile(vodFile, newVodFile);
        } catch (IOException e) {
            throw new StorageException("Failed to move vod from temporary directory to output directory", e);
        }

        ClipOptions vodMetadata;
        try {
            vodMetadata = metadataService.getVideoMetadata(newVodFile).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new FFMPEGException("Error retrieving video metadata for vod: " + e.getMessage());
        }

        try {
            thumbnailService.createThumbnail(newVodFile, thumbnailFile, 0.0f);
        } catch (InterruptedException e) {
            logger.error("Thumbnail generation interrupted for user: {}", user.getId(), e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.error("Error generating thumbnail for user: {}", user.getId(), e);
        }

        vodMetadata.setTitle(title);
        vodMetadata.setDescription(description);

        Vod vod = saveToDb(vodMetadata, user, newVodFile, thumbnailFile);
        logger.info("Vod created successfully with ID: {}", vod.getId());
    }

    private Vod saveToDb(ClipOptions options,
                         User user,
                         Path videoPath,
                         Path thumbnailPath) {
        Vod vod = new Vod();
        vod.setUser(user);
        vod.setTitle(options.getTitle() != null ? options.getTitle() : "Untitled VoD");
        vod.setDescription(options.getDescription() != null ? options.getDescription() : "");
        vod.setCreatedAt(Instant.now());
        vod.setWidth(options.getWidth());
        vod.setHeight(options.getHeight());
        vod.setFps(options.getFps());
        vod.setDuration(options.getDuration());
        vod.setFileSize(options.getFileSize());
        vod.setVideoPath(directoryService.relativisePath(videoPath.toAbsolutePath()).toString());
        vod.setThumbnailPath(directoryService.relativisePath(thumbnailPath.toAbsolutePath()).toString());
        return vodRepository.save(vod);
    }

    private void deleteVodFiles(Vod vod) {
        File vodFile = new File(vod.getVideoPath());
        File thumbnailFile = new File(vod.getThumbnailPath());

        try {
            Files.deleteIfExists(vodFile.toPath());
            Files.deleteIfExists(thumbnailFile.toPath());
        } catch (IOException e) {
            logger.error("Could not delete vod files for vod ID: {}", vod.getId());
        }
    }

    private boolean isAuthenticatedForVod(Vod vod) {
        Optional<User> user = userService.getLoggedInUser();

        if (vod == null || user.isEmpty()) {
            return false;
        }

        return user.get().getId().equals(vod.getUser().getId());
    }
}
