package com.ddf.vodsystem.services.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class ThumbnailService {
    private static final Logger logger = LoggerFactory.getLogger(ThumbnailService.class);

    @Async("ffmpegTaskExecutor")
    public void createThumbnail(Path inputFile, Path outputFile, Float timeInVideo) throws IOException, InterruptedException {
        logger.info("Creating thumbnail at {} seconds", timeInVideo);

        List<String> command = List.of(
                "ffmpeg",
                "-ss", timeInVideo.toString(),
                "-i", inputFile.toAbsolutePath().toString(),
                "-frames:v", "1",
                outputFile.toAbsolutePath().toString()
        );

        CommandRunner.run(command);
    }
}
