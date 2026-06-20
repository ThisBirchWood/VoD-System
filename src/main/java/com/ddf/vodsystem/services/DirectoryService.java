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

    private static final long TEMP_DIR_TIME_LIMIT = 3 * 60 * 60 * (long) 1000; // 3 hours
    private static final long TEMP_DIR_CLEANUP_RATE = 30 * 60 * (long) 1000; // 30 minutes

    public File getTempInputFile(String filename) {
        String dir = tempInputsDir + File.separator + filename;
        return new File(dir);
    }

    public File getTempOutputFile(String filename) {
        String dir = tempOutputsDir + File.separator + filename;
        return new File(dir);
    }

    public File getUserFolder(Long userId) throws IOException {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        String dir = outputDir + File.separator + userId;
        createDirectory(dir);
        return new File(dir);
    }

    public File getUserClipsFile(Long userId, String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        File userDir = getUserFolder(userId);
        String fullPath = userDir.getAbsolutePath() + File.separator + "clips" + File.separator + filename;
        File file = new File(fullPath);

        createDirectory(file.getParent());
        return file;
    }

    public File getVodFile(Long userId, String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        String userDir = getUserFolder(userId).getAbsolutePath();
        File file = new File(userDir + File.separator + "vods" + File.separator + filename);
        createDirectory(file.getParent());

        return file;
    }

    public File getUserThumbnailsFile(Long userId, String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        String userDir = getUserFolder(userId).getAbsolutePath();
        File file = new File(userDir + File.separator + "thumbnails" + File.separator + filename);
        createDirectory(file.getParent());

        return file;
    }

    public void saveMultipartFile(File file, MultipartFile multipartFile) throws IOException {
        createDirectory(file.getParent());
        Path filePath = Paths.get(file.getAbsolutePath());
        Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    public void copyFile(File source, File target) throws IOException {
        Path sourcePath = Paths.get(source.getAbsolutePath());
        Path destPath = Paths.get(target.getAbsolutePath());

        Files.createDirectories(destPath.getParent());
        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Copied file from {} to {}", sourcePath, destPath);
    }

    public void cutFile(File source, File target) throws IOException {
        copyFile(source, target);
        Files.deleteIfExists(source.toPath());
        logger.info("Deleted source file: {}", source.getAbsolutePath());
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
            if (f.isFile() && f.lastModified() < (System.currentTimeMillis() - TEMP_DIR_TIME_LIMIT)) {
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