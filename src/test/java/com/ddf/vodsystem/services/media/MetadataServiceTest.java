package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.dto.ClipOptions;
import com.ddf.vodsystem.dto.CommandOutput;
import com.ddf.vodsystem.exceptions.FFMPEGException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {

    @Mock
    private CommandRunner commandRunner;

    private MetadataService metadataService;

    private static final Path INPUT = Path.of("/tmp/input.mp4");

    @BeforeEach
    void setUp() {
        metadataService = new MetadataService(commandRunner);
        Thread.interrupted(); // clear any flag leaked from a previous test
    }

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    /** Wraps a raw ffprobe JSON payload into a successful CommandOutput. */
    private CommandOutput ffprobeOutput(String json) {
        CommandOutput output = new CommandOutput();
        output.addLine(json);
        return output;
    }

    private ClipOptions runMetadata() throws Exception {
        return metadataService.getVideoMetadata(INPUT).get();
    }

    private void assertMetadataFailsWith(String messageFragment) {
        assertThatThrownBy(() -> metadataService.getVideoMetadata(INPUT).get())
                .satisfies(thrown -> {
                    Throwable cause = thrown instanceof ExecutionException ? thrown.getCause() : thrown;
                    assertThat(cause).isInstanceOf(FFMPEGException.class);
                    assertThat(cause.getMessage()).contains(messageFragment);
                });
    }

    // ---------------------------------------------------------------
    // Happy path / parsing behavior
    // ---------------------------------------------------------------

    @Test
    void getVideoMetadata_validJson_parsesAllFields() throws Exception {
        String json = """
                {
                    "streams": [
                        {
                            "width": 1920,
                            "height": 1080,
                            "r_frame_rate": "30/1",
                            "duration": "60.000000"
                        }
                    ],
                    "format": {
                        "size": "10485760",
                        "duration": "60.000000"
                    }
                }
                """;
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        ClipOptions options = runMetadata();

        assertThat(options.getStartPoint()).isEqualTo(0f);
        assertThat(options.getDuration()).isEqualTo(60.0f);
        assertThat(options.getWidth()).isEqualTo(1920);
        assertThat(options.getHeight()).isEqualTo(1080);
        assertThat(options.getFps()).isEqualTo(30.0f);
        assertThat(options.getFileSize()).isEqualTo(10485760f);
    }

    @Test
    void getVideoMetadata_ffprobeCommandIsCorrect() throws Exception {
        String json = """
                {
                    "streams": [ { "width": 1920, "height": 1080, "r_frame_rate": "30/1", "duration": "60.0" } ],
                    "format": { "size": "1000", "duration": "60.0" }
                }
                """;
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        runMetadata();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(commandRunner).run(captor.capture());
        List<String> command = captor.getValue();

        assertThat(command).startsWith("ffprobe");
        assertThat(command).containsSequence("-print_format", "json");
        assertThat(command).containsSequence("-select_streams", "v:0");
        assertThat(command).containsSequence("-i", INPUT.toAbsolutePath().toString());
    }

    @Test
    void getVideoMetadata_fractionalFrameRate_computesFps() throws Exception {
        String json = """
                {
                    "streams": [ { "width": 1280, "height": 720, "r_frame_rate": "30000/1001", "duration": "10.0" } ],
                    "format": { "size": "500", "duration": "10.0" }
                }
                """;
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        ClipOptions options = runMetadata();

        assertThat(options.getFps()).isCloseTo(29.97f, org.assertj.core.data.Offset.offset(0.01f));
    }

    @Test
    void getVideoMetadata_integerFrameRate_parsesFps() throws Exception {
        String json = """
                {
                    "streams": [ { "width": 1280, "height": 720, "r_frame_rate": "25", "duration": "10.0" } ],
                    "format": { "size": "500", "duration": "10.0" }
                }
                """;
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        ClipOptions options = runMetadata();

        assertThat(options.getFps()).isEqualTo(25.0f);
    }

    @Test
    void getVideoMetadata_zeroDenominatorFrameRate_fpsIsNull() throws Exception {
        String json = """
                {
                    "streams": [ { "width": 1280, "height": 720, "r_frame_rate": "0/0", "duration": "10.0" } ],
                    "format": { "size": "500", "duration": "10.0" }
                }
                """;
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        ClipOptions options = runMetadata();

        assertThat(options.getFps()).isNull();
    }

    @Test
    void getVideoMetadata_missingFrameRate_fpsIsNull() throws Exception {
        String json = """
                {
                    "streams": [ { "width": 1280, "height": 720, "duration": "10.0" } ],
                    "format": { "size": "500", "duration": "10.0" }
                }
                """;
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        ClipOptions options = runMetadata();

        assertThat(options.getFps()).isNull();
    }

    @Test
    void getVideoMetadata_missingStreams_throwsFFMPEGException() throws Exception {
        String json = """
                {
                    "streams": [],
                    "format": { "size": "500", "duration": "10.0" }
                }
                """;
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        assertMetadataFailsWith("streams missing");
    }

    @Test
    void getVideoMetadata_missingWidth_throwsFFMPEGException() throws Exception {
        String json = """
                {
                    "streams": [ { "height": 720, "r_frame_rate": "25", "duration": "10.0" } ],
                    "format": { "size": "500", "duration": "10.0" }
                }
                """;
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        assertMetadataFailsWith("width missing");
    }

    @Test
    void getVideoMetadata_missingHeight_throwsFFMPEGException() throws Exception {
        String json = """
                {
                    "streams": [ { "width": 1280, "r_frame_rate": "25", "duration": "10.0" } ],
                    "format": { "size": "500", "duration": "10.0" }
                }
                """;
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        assertMetadataFailsWith("height missing");
    }

    @Test
    void getVideoMetadata_missingFileSize_throwsFFMPEGException() throws Exception {
        String json = """
                {
                    "streams": [ { "width": 1280, "height": 720, "r_frame_rate": "25", "duration": "10.0" } ],
                    "format": { "duration": "10.0" }
                }
                """;
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        assertMetadataFailsWith("file size missing");
    }

    @Test
    void getVideoMetadata_durationMissingEverywhere_throwsFFMPEGException() throws Exception {
        String json = """
                {
                    "streams": [ { "width": 1280, "height": 720, "r_frame_rate": "25" } ],
                    "format": { "size": "500" }
                }
                """;
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        assertMetadataFailsWith("duration missing");
    }

    @Test
    void getVideoMetadata_commandThrowsIOException_throwsFFMPEGException() throws Exception {
        when(commandRunner.run(anyList())).thenThrow(new IOException("ffprobe boom"));

        assertMetadataFailsWith("Error while getting video metadata");
    }

    // ---------------------------------------------------------------
    // Design/best-practice expectations.
    //
    // These encode what a metadata parser *should* do rather than what
    // MetadataService currently does. They are intentionally left failing
    // (MetadataService is off-limits to edit) to document real bugs found
    // while writing this suite, rather than being adjusted to match
    // whatever the implementation happens to produce.
    // ---------------------------------------------------------------

    @Test
    void getVideoMetadata_streamMissingDuration_fallsBackToFormatDuration() throws Exception {
        // format=duration is requested from ffprobe specifically as a fallback source
        // (see extractEndPointFromFormat), but extractDuration() throws as soon as the
        // stream lacks "duration" -- before that fallback is ever reached. The fallback
        // is currently dead code. A correct implementation should recover here instead
        // of failing a request whose format block has perfectly good duration data.
        String json = """
                {
                    "streams": [ { "width": 1280, "height": 720, "r_frame_rate": "25" } ],
                    "format": { "size": "500", "duration": "45.5" }
                }
                """;
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        ClipOptions options = runMetadata();

        assertThat(options.getDuration()).isEqualTo(45.5f);
    }

    @Test
    void getVideoMetadata_largeFileSize_doesNotLosePrecision() throws Exception {
        // extractFileSize() parses byte counts into a 32-bit float, which can only
        // represent integers exactly up to 2^24 (16,777,216). Any real video file
        // larger than ~16MB gets its reported size silently rounded. A metadata
        // service should preserve exact byte counts (e.g. via long/double), not
        // lose precision on ordinary file sizes.
        long exactBytes = 16_777_217L; // smallest integer a float cannot represent exactly
        String json = """
                {
                    "streams": [ { "width": 1280, "height": 720, "r_frame_rate": "25", "duration": "10.0" } ],
                    "format": { "size": "%d", "duration": "10.0" }
                }
                """.formatted(exactBytes);
        when(commandRunner.run(anyList())).thenReturn(ffprobeOutput(json));

        ClipOptions options = runMetadata();

        double reportedBytes = options.getFileSize();
        assertThat(reportedBytes).isEqualTo((double) exactBytes);
    }

    @Test
    void getVideoMetadata_plainIOException_doesNotSetThreadInterruptFlag() throws Exception {
        // The catch block treats IOException and InterruptedException identically and
        // calls Thread.currentThread().interrupt() for both. That's correct for an
        // actual InterruptedException (see the companion test below) but wrong for a
        // plain IOException -- e.g. ffprobe exiting non-zero -- which has nothing to do
        // with thread interruption. Spuriously setting the flag can cause unrelated
        // interruption checks elsewhere (in the same worker thread/pool) to misfire.
        when(commandRunner.run(anyList())).thenThrow(new IOException("ffprobe boom"));

        assertMetadataFailsWith("Error while getting video metadata");

        assertThat(Thread.currentThread().isInterrupted())
                .as("a plain IOException must not leave the calling thread's interrupt flag set")
                .isFalse();
    }

    @Test
    void getVideoMetadata_interruptedException_setsThreadInterruptFlag() throws Exception {
        // Contrast case: a real InterruptedException should restore the interrupt flag,
        // per standard Java concurrency practice. This one is expected to pass.
        when(commandRunner.run(anyList())).thenThrow(new InterruptedException("interrupted"));

        assertMetadataFailsWith("Error while getting video metadata");

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }
}
