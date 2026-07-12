package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.dto.CommandOutput;
import com.ddf.vodsystem.dto.ProgressTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamActionsServiceTest {

    @Mock
    private CommandRunner commandRunner;

    private StreamActionsService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new StreamActionsService(commandRunner);
    }

    private void createSegment(String name) throws IOException {
        Files.createFile(tempDir.resolve(name));
    }

    // ---------------------------------------------------------------
    // getSegmentsInRange: missing / invalid directories
    // ---------------------------------------------------------------

    @Test
    void getSegmentsInRange_directoryDoesNotExist_throwsIOException() {
        Path missing = tempDir.resolve("does-not-exist");

        assertThatThrownBy(() -> service.getSegmentsInRange(missing, Instant.EPOCH, Instant.now()))
                .isInstanceOf(NoSuchFileException.class);
    }

    @Test
    void getSegmentsInRange_pathIsAFileNotADirectory_throwsIOException() throws IOException {
        Path file = tempDir.resolve("not-a-directory.txt");
        Files.createFile(file);

        assertThatThrownBy(() -> service.getSegmentsInRange(file, Instant.EPOCH, Instant.now()))
                .isInstanceOf(IOException.class);
    }

    @Test
    void getSegmentsInRange_emptyDirectory_returnsEmptyList() throws IOException {
        List<Path> result = service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(0), Instant.ofEpochMilli(100_000));

        assertThat(result).isEmpty();
    }

    // ---------------------------------------------------------------
    // getSegmentsInRange: filename filtering
    // ---------------------------------------------------------------

    @Test
    void getSegmentsInRange_ignoresFilesNotEndingInTs() throws IOException {
        createSegment("1.ts");
        createSegment("playlist.m3u8");
        createSegment("1.ts.tmp");
        createSegment("readme.txt");

        List<Path> result = service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(0), Instant.ofEpochMilli(5000));

        assertThat(result).extracting(p -> p.getFileName().toString()).containsExactly("1.ts");
    }

    @Test
    void getSegmentsInRange_directoryNamedLikeASegment_isNotIncluded() throws IOException {
        Files.createDirectory(tempDir.resolve("5.ts"));

        List<Path> result = service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(0), Instant.ofEpochMilli(10_000));

        assertThat(result).isEmpty();
    }

    @Test
    void getSegmentsInRange_malformedNonNumericFilename_throwsNumberFormatException() throws IOException {
        // A real HLS fragment name is always numeric. A corrupted or partially-written
        // file (e.g. "index.ts") crashes the whole listing instead of just being skipped.
        createSegment("index.ts");

        assertThatThrownBy(() ->
                service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(0), Instant.ofEpochMilli(10_000)))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void getSegmentsInRange_emptyFilenameBeforeExtension_throwsNumberFormatException() throws IOException {
        createSegment(".ts");

        assertThatThrownBy(() ->
                service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(0), Instant.ofEpochMilli(10_000)))
                .isInstanceOf(NumberFormatException.class);
    }

    // ---------------------------------------------------------------
    // getSegmentsInRange: range-boundary arithmetic
    // ---------------------------------------------------------------

    @Test
    void getSegmentsInRange_segmentEndsExactlyAtRangeStart_isExcluded() throws IOException {
        // "0.ts" spans [0, 3000)ms. A request starting exactly at 3000ms should not
        // pull in a segment that ended the instant before.
        createSegment("0.ts");

        List<Path> result = service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(3000), Instant.ofEpochMilli(10_000));

        assertThat(result).isEmpty();
    }

    @Test
    void getSegmentsInRange_segmentOverlapsRangeStartByOneMs_isIncluded() throws IOException {
        createSegment("0.ts"); // spans [0, 3000)

        List<Path> result = service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(2999), Instant.ofEpochMilli(10_000));

        assertThat(result).hasSize(1);
    }

    @Test
    void getSegmentsInRange_segmentStartsExactlyAtRangeEnd_isExcluded() throws IOException {
        createSegment("5.ts"); // starts at 5000ms

        List<Path> result = service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(0), Instant.ofEpochMilli(5000));

        assertThat(result).isEmpty();
    }

    @Test
    void getSegmentsInRange_segmentStartsOneMsBeforeRangeEnd_isIncluded() throws IOException {
        createSegment("5.ts"); // starts at 5000ms

        List<Path> result = service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(0), Instant.ofEpochMilli(5001));

        assertThat(result).hasSize(1);
    }

    @Test
    void getSegmentsInRange_startAfterEnd_throwsIllegalArgument() throws IOException {
        createSegment("10.ts");

        assertThatThrownBy(() ->
                service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(20_000), Instant.ofEpochMilli(10_000)))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void getSegmentsInRange_segmentsWhollyOutsideRange_areExcluded() throws IOException {
        createSegment("0.ts");   // [0, 3000)
        createSegment("100.ts"); // [100000, 103000)

        List<Path> result = service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(50_000), Instant.ofEpochMilli(60_000));

        assertThat(result).isEmpty();
    }

    @Test
    void getSegmentsInRange_segmentsOutOfOrder_returnedInOrder() throws IOException {
        createSegment("20.ts");
        createSegment("10.ts");
        createSegment("0.ts");

        List<Path> result = service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(0), Instant.ofEpochMilli(25_000));

        assertThat(result)
                .extracting(p -> p.getFileName().toString())
                .containsExactly("0.ts", "10.ts", "20.ts");
    }

    @Test
    void getSegmentsInRange_mixOfOverlappingAndNonOverlappingSegments_returnsOnlyOverlapping() throws IOException {
        createSegment("0.ts"); // [0, 3000)
        createSegment("3.ts"); // [3000, 6000)
        createSegment("6.ts"); // [6000, 9000)
        createSegment("9.ts"); // [9000, 12000)


        List<Path> result = service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(4000), Instant.ofEpochMilli(8000));

        assertThat(result).extracting(p -> p.getFileName().toString())
                .containsExactly("3.ts", "6.ts");
    }

    @Test
    void getSegmentsInRange_resultsAreSortedByTimestampRegardlessOfDirectoryOrder() throws IOException {
        createSegment("30.ts");
        createSegment("10.ts");
        createSegment("20.ts");

        List<Path> result = service.getSegmentsInRange(tempDir, Instant.ofEpochMilli(0), Instant.ofEpochMilli(100_000));

        assertThat(result).extracting(p -> p.getFileName().toString())
                .containsExactly("10.ts", "20.ts", "30.ts");
    }

    // ---------------------------------------------------------------
    // parseTimestampMs: unit-based conversion
    // ---------------------------------------------------------------

    @Test
    void parseTimestampMs_secondsBasedName_convertsToMillis() {
        long result = service.parseTimestampMs(Path.of("42.ts"));

        assertThat(result).isEqualTo(42_000L);
    }

    @Test
    void parseTimestampMs_millisecondBasedName_usedAsIs() {
        long result = service.parseTimestampMs(Path.of("1700000000000.ts"));

        assertThat(result).isEqualTo(1_700_000_000_000L);
    }

    @Test
    void parseTimestampMs_valueExactlyAtUnitThreshold_treatedAsMillis() {
        long result = service.parseTimestampMs(Path.of("1000000000000.ts"));

        assertThat(result).isEqualTo(1_000_000_000_000L);
    }

    @Test
    void parseTimestampMs_valueOneBelowUnitThreshold_treatedAsSecondsAndMultiplied() {
        long result = service.parseTimestampMs(Path.of("999999999999.ts"));

        assertThat(result).isEqualTo(999_999_999_999_000L);
    }

    @Test
    void parseTimestampMs_nonNumericName_throwsNumberFormatException() {
        assertThatThrownBy(() -> service.parseTimestampMs(Path.of("abc.ts")))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void parseTimestampMs_filenameContainsExtraTsSubstring_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.parseTimestampMs(Path.of("1.ts5.ts")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------------------------------------------------------------
    // saveSection
    // ---------------------------------------------------------------

    @Test
    void saveSection_success_buildsExpectedCommandAndCompletesWithOutputFile() throws Exception {
        Path segmentA = Path.of("/tmp/streams/a.ts");
        Path segmentB = Path.of("/tmp/streams/b.ts");
        Path output = Path.of("/tmp/out/clip.mp4");
        ProgressTracker progress = new ProgressTracker();

        when(commandRunner.run(anyList(), any())).thenReturn(new CommandOutput());

        var future = service.saveSection(List.of(segmentA, segmentB), 5.0f, 12.0f, output, progress);

        assertThat(future.get()).isEqualTo(output);
        assertThat(progress.isComplete()).isTrue();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(commandRunner).run(captor.capture(), any());
        List<String> command = captor.getValue();

        assertThat(command).containsSequence("-i",
                "concat:" + segmentA.toAbsolutePath() + "|" + segmentB.toAbsolutePath());
        assertThat(command).containsSequence("-ss", "5.0");
        assertThat(command).containsSequence("-t", "12.0");
        assertThat(command).containsSequence("-c", "copy");
        assertThat(command).endsWith(output.toAbsolutePath().toString());
    }

    @Test
    void saveSection_commandRunnerThrowsIOException_completesExceptionallyWithoutMarkingProgressComplete() throws Exception {
        Path output = Path.of("/tmp/out/clip.mp4");
        ProgressTracker progress = new ProgressTracker();

        when(commandRunner.run(anyList(), any())).thenThrow(new IOException("ffmpeg boom"));

        var future = service.saveSection(List.of(Path.of("/tmp/a.ts")), 0f, 10f, output, progress);

        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(IOException.class);
        assertThat(progress.isComplete()).isFalse();
    }

    @Test
    void saveSection_emptySegmentList_throwsIllegalStateException() {
        Path output = Path.of("/tmp/out/clip.mp4");
        ProgressTracker progress = new ProgressTracker();

        var future = service.saveSection(List.of(), 0f, 10f, output, progress);

        assertThatThrownBy(future::get).isInstanceOf(ExecutionException.class);

    }

    @Test
    void saveSection_nullOutputFile_completesExceptionally() {
        ProgressTracker progress = new ProgressTracker();

        var future = service.saveSection(List.of(Path.of("/tmp/a.ts")), 0f, 10f, null, progress);

        assertThatThrownBy(future::get).isInstanceOf(ExecutionException.class);
    }
}
