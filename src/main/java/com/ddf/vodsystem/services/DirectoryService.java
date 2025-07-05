package com.ddf.vodsystem.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
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

    public File getTempInputFile(String id) {
        String dir = tempInputsDir + File.separator + id;
        return new File(dir);
    }

    public File getTempOutputFile(String id) {
        String dir = tempOutputsDir + File.separator + id;
        return new File(dir);
    }

    public File getOutputFile(String id) {
        String dir = outputDir + File.separator + id;
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

    public void createDirectory(String dir) throws IOException {
        // Create the directory if it doesn't exist
        Path outputPath = Paths.get(dir);
        if (Files.notExists(outputPath)) {
            Files.createDirectories(outputPath);
            logger.info("Created directory: {}", outputPath);
        }
    }

    @PostConstruct
    public void createDirectoriesIfNotExist() throws IOException {
        createDirectory(tempInputsDir);
        createDirectory(tempOutputsDir);
        createDirectory(outputDir);
    }
}