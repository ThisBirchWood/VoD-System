package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.Job;
import com.ddf.vodsystem.dto.VideoMetadata;
import com.ddf.vodsystem.exceptions.FFMPEGException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
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
    private final FfmpegService ffmpegService;

    @Autowired
    public UploadService(JobService jobService,
                         MetadataService metadataService,
                         DirectoryService directoryService,
                         FfmpegService ffmpegService) {
        this.jobService = jobService;
        this.metadataService = metadataService;
        this.directoryService = directoryService;
        this.ffmpegService = ffmpegService;
    }

    public String upload(MultipartFile file) {
        // generate uuid, filename
        String uuid = generateShortUUID();

        File inputFile = directoryService.getTempInputFile(uuid + ".mp4");
        File outputFile = directoryService.getTempOutputFile(uuid + ".mp4");

        // convert to mp4
        if (!isMp4File(file)) {
            String originalFilename = file.getOriginalFilename();

            logger.info("File is not in MP4 format, converting: {}", file.getOriginalFilename());
            File tempFile = directoryService.getTempInputFile(originalFilename);
            directoryService.saveMultipartFile(tempFile, file);

            convertToMp4(tempFile, inputFile);
        } else {
            logger.info("File is already in MP4 format: {}", file.getOriginalFilename());
            directoryService.saveMultipartFile(inputFile, file);
        }

        // add job
        logger.info("Uploaded file and creating job with UUID: {}", uuid);
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

    private void convertToMp4(File inputFile, File outputFile) {
        try {
            ffmpegService.remux(inputFile, outputFile);
        } catch (IOException e) {
            throw new FFMPEGException("Error during file conversion" + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FFMPEGException("FFmpeg process was interrupted" + e.getMessage());
        } finally {
            if (!inputFile.delete()) {
                logger.warn("Failed to delete temporary file: {}", inputFile.getAbsolutePath());
            }
        }
    }

    public static boolean isMp4File(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        return contentType != null && contentType.equals("video/mp4");
    }

}
