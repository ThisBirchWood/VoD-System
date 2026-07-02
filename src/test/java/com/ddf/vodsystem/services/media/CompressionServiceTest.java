package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.dto.ClipOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompressionServiceTest {
    @Mock
    private CommandRunner commandRunner;

    private CompressionService compressionService;

    @BeforeEach
    void setUp() {
        compressionService = new CompressionService(commandRunner);
    }

    @Test
    void buildCommand_minimalClipOptions_smallCommand() {
        Path input = Path.of("/tmp/input.mp4");
        Path output = Path.of("/tmp/output.mp4");
        ClipOptions options = new ClipOptions();
        options.setTitle("hello");
        options.setStartPoint(10.0f);
        options.setDuration(30.0f);

        List<String> command = compressionService.buildCommand(input, output, options);

        assertThat(command).containsSubsequence("-ss", "10.0");
        assertThat(command).containsSubsequence("-t", "30.0");
        assertThat(command).containsSubsequence("-i", input.toAbsolutePath().toString());

        assertThat(command).doesNotContain("-vf");
        assertThat(command).doesNotContain("-b:v");

        assertThat(command).endsWith(output.toAbsolutePath().toString());
    }

    @Test
    void buildCommand_hasFileSize_hasBitrateOptions() {
        Path input = Path.of("/tmp/input.mp4");
        Path output = Path.of("/tmp/output.mp4");
        ClipOptions options = new ClipOptions();

        options.setTitle("hello");
        options.setStartPoint(10.0f);
        options.setDuration(30.0f);
        options.setFileSize(10.0f);

        List<String> command = compressionService.buildCommand(input, output, options);

        assertThat(command).contains("-b:v", "-b:a");
        int bvIndex = command.indexOf("-b:v");
        int baIndex = command.indexOf("-b:a");

        assertThat(command.get(bvIndex + 1)).isNotBlank().endsWith("k");
        assertThat(command.get(baIndex + 1)).isNotBlank().endsWith("k");
    }

    @Test
    void buildCommand_hasWidthHeight_hasVideoFilters() {
        Path input = Path.of("/tmp/input.mp4");
        Path output = Path.of("/tmp/output.mp4");
        ClipOptions options = new ClipOptions();

        options.setTitle("hello");
        options.setStartPoint(10.0f);
        options.setDuration(30.0f);
        options.setWidth(1920);
        options.setHeight(1080);

        List<String> command = compressionService.buildCommand(input, output, options);

        assertThat(command).containsSequence("-vf", "scale=1920:1080");
    }

    @Test
    void buildCommand_onlyWidth_aspectRatioKept() {
        Path input = Path.of("/tmp/input.mp4");
        Path output = Path.of("/tmp/output.mp4");
        ClipOptions options = new ClipOptions();

        options.setTitle("hello");
        options.setStartPoint(10.0f);
        options.setDuration(30.0f);
        options.setWidth(1920);

        List<String> command = compressionService.buildCommand(input, output, options);

        assertThat(command).containsSequence("-vf", "scale=1920:-1");
    }

    @Test
    void buildCommand_onlyHeight_aspectRatioKept() {
        Path input = Path.of("/tmp/input.mp4");
        Path output = Path.of("/tmp/output.mp4");
        ClipOptions options = new ClipOptions();

        options.setTitle("hello");
        options.setStartPoint(10.0f);
        options.setDuration(30.0f);
        options.setHeight(1080);

        List<String> command = compressionService.buildCommand(input, output, options);

        assertThat(command).containsSequence("-vf", "scale=-1:1080");
    }

    @Test
    void buildCommand_onlyHasFps_hasNoScaleFilter() {
        Path input = Path.of("/tmp/input.mp4");
        Path output = Path.of("/tmp/output.mp4");
        ClipOptions options = new ClipOptions();

        options.setTitle("hello");
        options.setStartPoint(10.0f);
        options.setDuration(30.0f);
        options.setFps(60f);

        List<String> command = compressionService.buildCommand(input, output, options);

        assertThat(command).containsSequence("-vf", "fps=60.0");
    }

    @Test
    void buildCommand_noInputFile_throwsNullPointer() {
        Path output = Path.of("/tmp/output.mp4");
        ClipOptions options = new ClipOptions();

        options.setTitle("hello");
        options.setStartPoint(10.0f);
        options.setDuration(30.0f);

        assertThrows(NullPointerException.class, () ->
                compressionService.buildCommand(null, output, options)
        );
    }

    @Test
    void buildCommand_noOutputFile_throwsNullPointer() {
        Path input = Path.of("/tmp/output.mp4");
        ClipOptions options = new ClipOptions();

        options.setTitle("hello");
        options.setStartPoint(10.0f);
        options.setDuration(30.0f);

        assertThrows(NullPointerException.class, () ->
                compressionService.buildCommand(input, null, options)
        );
    }
}