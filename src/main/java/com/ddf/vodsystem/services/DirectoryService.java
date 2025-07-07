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

    public File getTempInputFile(String id, String extension) {
        String dir = tempInputsDir + File.separator + id + (extension.isEmpty() ? "" : "." + extension);
        return new File(dir);
    }

    public File getTempOutputFile(String id, String extension) {
        String dir = tempOutputsDir + File.separator + id + (extension.isEmpty() ? "" : "." + extension);
        return new File(dir);
    }

    public File getOutputFile(String id, String extension) {
        String dir = outputDir + File.separator + id + (extension.isEmpty() ? "" : "." + extension);
        return new File(dir);
    }

    public void saveData(File file, MultipartFile multipartFile) {
        try {
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
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Copied file from {} to {}", sourcePath, destPath);
        } catch (IOException e) {
            logger.error(e.getMessage());
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

    @Scheduled(fixedRate = (30 * 60 * 1000))
    public void cleanTempDirectories() throws IOException {
        cleanUpDirectory(tempInputsDir);
        cleanUpDirectory(tempOutputsDir);
    }
}