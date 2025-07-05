package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.Job;
import com.ddf.vodsystem.entities.VideoMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UploadService {
    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    private final JobService jobService;
    private final MetadataService metadataService;
    private final DirectoryService directoryService;

    @Autowired
    public UploadService(JobService jobService,
                         MetadataService metadataService,
                         DirectoryService directoryService) {
        this.jobService = jobService;
        this.metadataService = metadataService;
        this.directoryService = directoryService;
    }

    public String upload(MultipartFile file) {
        // generate uuid, filename
        String uuid = generateShortUUID();
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = uuid + (extension.isEmpty() ? "" : "." + extension);

        File inputFile = directoryService.getTempInputFile(filename);
        File outputFile = directoryService.getTempOutputFile(filename);
        directoryService.saveData(inputFile, file);

        // add job
        VideoMetadata videoMetadata = metadataService.getVideoMetadata(inputFile);
        Job job = new Job(uuid, inputFile, outputFile, videoMetadata);
        jobService.add(job);

        return uuid;
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
}
