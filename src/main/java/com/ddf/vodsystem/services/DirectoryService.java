package com.ddf.vodsystem.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;

@Service
public class DirectoryService {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DirectoryService.class);

    @Value("${storage.outputs}")
    private String outputDir;

    @Value("${storage.temp.inputs}")
    private String tempInputsDir;

    @Value("${storage.temp.outputs}")
    private String tempOutputsDir;

    private static final long TEMP_DIR_TIMELIMIT = 3 * 60 * 60 * (long) 1000; // 3 hours
    private static final long TEMP_DIR_CLEANUP_RATE = 30 * 60 * (long) 1000; // 30 minutes

    public File getTempInputFile(String id, String extension) {
        String dir = tempInputsDir + File.separator + id + (extension.isEmpty() ? "" : "." + extension);
        return new File(dir);
    }

    public File getTempOutputFile(String id, String extension) {
        String dir = tempOutputsDir + File.separator + id + (extension.isEmpty() ? "" : "." + extension);
        return new File(dir);
    }

    public File getUserClipsDir(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        String dir = outputDir + File.separator + userId + File.separator + "clips";
        return new File(dir);
    }

    public File getUserThumbnailsDir(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        String dir = outputDir + File.separator + userId + File.separator + "thumbnails";
        File thumbnailDir = new File(dir);

        try {
            createDirectory(thumbnailDir.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error creating thumbnails directory: {}", e.getMessage());
        }

        return thumbnailDir;
    }

    public File getUserClipsFile(Long userId, String fileName) {
        if (userId == null || fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("User ID and file name cannot be null or empty");
        }

        String dir = outputDir + File.separator + userId + File.separator + "clips" + File.separator + fileName;
        File file = new File(dir);

        try {
            createDirectory(file.getParent());
        } catch (IOException e) {
            logger.error("Error creating clips directory: {}", e.getMessage());
        }

        return file;
    }

    public File getUserThumbnailsFile(Long userId, String fileName) {
        if (userId == null || fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("User ID and file name cannot be null or empty");
        }

        String dir = outputDir + File.separator + userId + File.separator + "thumbnails" + File.separator + fileName;
        File file = new File(dir);

        try {
            createDirectory(file.getParent());
        } catch (IOException e) {
            logger.error("Error creating thumbnails directory: {}", e.getMessage());
        }

        return file;
    }

    public void saveAtDir(File file, MultipartFile multipartFile) {
        try {
            createDirectory(file.getAbsolutePath());
            Path filePath = Paths.get(file.getAbsolutePath());
            Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void copyFile(File source, File target) {
        Path sourcePath = Paths.get(source.getAbsolutePath());
        Path destPath = Paths.get(target.getAbsolutePath());

        try {
            Files.createDirectories(destPath.getParent());
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied file from {} to {}", sourcePath, destPath);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void cutFile(File source, File target) {
        copyFile(source, target);

        try {
            Files.deleteIfExists(source.toPath());
            logger.info("Deleted source file: {}", source.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error deleting source file: {}", e.getMessage());
        }
    }

    public boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            logger.warn("File does not exist: {}", file);
            return false;
        }

        try {
            Files.delete(file.toPath());
            logger.info("Deleted file: {}", file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            logger.error("Error deleting file: {}", e.getMessage());
            return false;
        }
    }

    public String getFileExtension(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return ""; // No extension
        }
        return fileName.substring(dotIndex + 1);
    }

    private void createDirectory(String dir) throws IOException {
        // Create the directory if it doesn't exist
        Path outputPath = Paths.get(dir);
        if (Files.notExists(outputPath)) {
            Files.createDirectories(outputPath);
            logger.info("Created directory: {}", outputPath);
        }
    }

    private void cleanUpDirectory(String dir) throws IOException {
        File file = new File(dir);
        File[] files = file.listFiles();

        if (files == null) {
            logger.warn("No files found in directory: {}", dir);
            return;
        }

        for (File f : files){
            if (f.isFile() && f.lastModified() < (System.currentTimeMillis() - TEMP_DIR_TIMELIMIT)) {
                Files.delete(f.toPath());
            }
        }
    }

    @PostConstruct
    public void createDirectoriesIfNotExist() throws IOException {
        createDirectory(tempInputsDir);
        createDirectory(tempOutputsDir);
        createDirectory(outputDir);
    }

    @Scheduled(fixedRate = TEMP_DIR_CLEANUP_RATE)
    public void cleanTempDirectories() throws IOException {
        cleanUpDirectory(tempInputsDir);
        cleanUpDirectory(tempOutputsDir);
    }
}