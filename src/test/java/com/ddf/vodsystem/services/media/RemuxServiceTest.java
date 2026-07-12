package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.dto.ProgressTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RemuxServiceTest {

    @Mock
    private CommandRunner commandRunner;

    private RemuxService remuxService;

    @BeforeEach
    void setUp() {
        remuxService = new RemuxService(commandRunner);
    }

    @Test
    void remux_buildsExpectedFfmpegCommand() throws Exception {
        File input = new File("videos/input.ts");
        File output = new File("videos/output.mp4");

        remuxService.remux(input, output, new ProgressTracker(), 60f);

        verify(commandRunner).run(eq(List.of(
                "ffmpeg",
                "-progress", "pipe:1",
                "-y",
                "-i", input.getAbsolutePath(),
                "-c:v", "h264",
                "-c:a", "aac",
                "-f", "mp4",
                output.getAbsolutePath()
        )), any());
    }
}
