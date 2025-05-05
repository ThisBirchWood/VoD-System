package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.Job;
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

@Service
public class UploadService {
    private static final String UPLOAD_DIR = "videos/";

    private JobService jobService;

    public UploadService(JobService jobService) {
        this.jobService = jobService;
    }

    public String upload(MultipartFile file) {
        // generate uuid, file
        String uuid = generateShortUUID();
        File uploadDir = new File(UPLOAD_DIR + uuid + ".mp4");
        moveToFile(file, uploadDir);

        // add job
        Job job = new Job(uuid, uploadDir);
        jobService.addJob(job);

        return uuid;
    }

    private void moveToFile(MultipartFile inputFile, File outputFile) {
        try {
            Path filePath = Paths.get(outputFile.getAbsolutePath());
            Files.copy(inputFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateShortUUID() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array());
    }
}
