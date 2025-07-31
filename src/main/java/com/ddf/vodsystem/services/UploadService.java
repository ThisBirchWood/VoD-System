package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.Job;
import com.ddf.vodsystem.dto.VideoMetadata;
import com.ddf.vodsystem.exceptions.FFMPEGException;
import com.ddf.vodsystem.services.media.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        String extension = directoryService.getFileExtension(file.getOriginalFilename());

        File inputFile = directoryService.getTempInputFile(uuid + "." + extension);
        File outputFile = directoryService.getTempOutputFile(uuid + ".mp4");
        directoryService.saveAtDir(inputFile, file);

        // add job
        logger.info("Uploaded file and creating job with UUID: {}", uuid);

        VideoMetadata videoMetadata;
        try {
            videoMetadata = metadataService.getVideoMetadata(inputFile).get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FFMPEGException(e.getMessage());
        }
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

}
