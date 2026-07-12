package com.ddf.vodsystem.services.media;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ThumbnailServiceTest {

    @Mock
    private CommandRunner commandRunner;

    private ThumbnailService thumbnailService;

    @BeforeEach
    void setUp() {
        thumbnailService = new ThumbnailService(commandRunner);
    }

    @Test
    void createThumbnail_buildsExpectedFfmpegCommand() throws Exception {
        Path input = Path.of("videos/input.mp4");
        Path output = Path.of("thumbnails/output.jpg");

        thumbnailService.createThumbnail(input, output, 12.5f);

        verify(commandRunner).run(List.of(
                "ffmpeg",
                "-ss", "12.5",
                "-i", input.toAbsolutePath().toString(),
                "-frames:v", "1",
                output.toAbsolutePath().toString()
        ));
    }
}