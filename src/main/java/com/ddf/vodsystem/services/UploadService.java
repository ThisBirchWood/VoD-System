package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.Job;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Endpoint
@AnonymousAllowed
public class UploadService {
    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    @Value("${temp.vod.storage}")
    private String inputDir;
    @Value("${temp.vod.output}")
    private String outputDir;

    private final JobService jobService;

    @Autowired
    public UploadService(JobService jobService) {
        this.jobService = jobService;
    }

    public String upload(MultipartFile file) {
        // generate uuid, filename
        String uuid = generateShortUUID();
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = uuid + (extension.isEmpty() ? "" : "." + extension);

        Path inputPath = Paths.get(inputDir, filename);
        File inputFile = inputPath.toFile();

        Path outputPath = Paths.get(outputDir, filename);
        File outputFile = outputPath.toFile();

        moveToFile(file, inputFile);

        // add job
        Job job = new Job(uuid, inputFile, outputFile);
        jobService.add(job);

        return uuid;
    }

    private void moveToFile(MultipartFile inputFile, File outputFile) {
        try {
            Path filePath = Paths.get(outputFile.getAbsolutePath());
            Files.copy(inputFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static String generateShortUUID() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array());
    }

    private static String getFileExtension(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return ""; // No extension
        }
        return fileName.substring(dotIndex + 1);
    }

    private void createDirectories() throws IOException {
        // Create INPUT_DIR if it doesn't exist
        Path inputDirPath = Paths.get(inputDir);
        if (Files.notExists(inputDirPath)) {
            Files.createDirectories(inputDirPath);
            logger.info("Created directory: {}", inputDir);
        }

        // Create OUTPUT_DIR if it doesn't exist
        Path outputDirPath = Paths.get(outputDir);
        if (Files.notExists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
            logger.info("Created directory: {}", outputDir);
        }
    }

    @PostConstruct
    public void init() {
        try {
            createDirectories();
        } catch (IOException e) {
            logger.error("Failed to create directories: " + e.getMessage(), e);
        }
    }
}
