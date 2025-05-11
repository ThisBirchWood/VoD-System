package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.Job;
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
public class UploadService {
    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    @Value("${temp.vod.storage}")
    private String INPUT_DIR;
    @Value("${temp.vod.output}")
    private String OUTPUT_DIR;

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

        Path inputPath = Paths.get(INPUT_DIR, filename);
        File inputFile = inputPath.toFile();

        Path outputPath = Paths.get(OUTPUT_DIR, filename);
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
        Path inputDirPath = Paths.get(INPUT_DIR);
        if (Files.notExists(inputDirPath)) {
            Files.createDirectories(inputDirPath);
            System.out.println("Created directory: " + INPUT_DIR);
        }

        // Create OUTPUT_DIR if it doesn't exist
        Path outputDirPath = Paths.get(OUTPUT_DIR);
        if (Files.notExists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
            System.out.println("Created directory: " + OUTPUT_DIR);
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
