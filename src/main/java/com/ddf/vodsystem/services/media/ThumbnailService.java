package com.ddf.vodsystem.services.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class ThumbnailService {
    private static final Logger logger = LoggerFactory.getLogger(ThumbnailService.class);

    @Async("ffmpegTaskExecutor")
    public void createThumbnail(File inputFile, File outputFile, Float timeInVideo) throws IOException, InterruptedException {
        logger.info("Creating thumbnail at {} seconds", timeInVideo);

        List<String> command = List.of(
                "ffmpeg",
                "-ss", timeInVideo.toString(),
                "-i", inputFile.getAbsolutePath(),
                "-frames:v", "1",
                outputFile.getAbsolutePath()
        );

        CommandRunner.run(command);
    }
}
