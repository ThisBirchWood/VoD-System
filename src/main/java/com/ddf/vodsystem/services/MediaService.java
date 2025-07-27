package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.CommandOutput;
import com.ddf.vodsystem.dto.ProgressTracker;
import com.ddf.vodsystem.dto.VideoMetadata;
import com.ddf.vodsystem.exceptions.FFMPEGException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tika.Tika;

@Service
public class MediaService {
    private static final Logger logger = LoggerFactory.getLogger(MediaService.class);
    private final Tika tika = new Tika();

    private static final float AUDIO_RATIO = 0.15f;
    private static final float MAX_AUDIO_BITRATE = 128f;
    private static final float BITRATE_MULTIPLIER = 0.9f;
    private static final String FFMPEG_PATH = "ffmpeg";
    private final Pattern timePattern = Pattern.compile("out_time_ms=(\\d+)");

    public void compress(File inputFile, File outputFile, VideoMetadata videoMetadata, ProgressTracker progress) throws IOException, InterruptedException {
        logger.info("Compressing video from {} to {}", inputFile.getAbsolutePath(), outputFile.getAbsolutePath());

        float length = videoMetadata.getEndPoint() - videoMetadata.getStartPoint();
        List<String> command = buildCommand(inputFile, outputFile, videoMetadata);
        CommandRunner.run(command, line -> setProgress(line, progress, length));
    }

    public void createThumbnail(File inputFile, File outputFile, Float timeInVideo) throws IOException, InterruptedException {
        logger.info("Creating thumbnail at {} seconds", timeInVideo);

        List<String> command = List.of(
                FFMPEG_PATH,
                "-ss", timeInVideo.toString(),
                "-i", inputFile.getAbsolutePath(),
                "-frames:v", "1",
                outputFile.getAbsolutePath()
        );

        CommandRunner.run(command);
    }

    public VideoMetadata getVideoMetadata(File file) {
        logger.info("Getting metadata for file {}", file.getAbsolutePath());

        List<String> command = List.of(
                "ffprobe",
                "-v", "quiet",
                "-print_format", "json",
                "-show_format", "-select_streams",
                "v:0", "-show_entries", "stream=duration,width,height,r_frame_rate:format=size,duration",
                "-i", file.getAbsolutePath()
        );

        ObjectMapper mapper = new ObjectMapper();
        StringBuilder outputBuilder = new StringBuilder();

        try {
            CommandOutput output = CommandRunner.run(command);

            for (String line : output.getOutput()) {
                outputBuilder.append(line);
            }

            JsonNode node = mapper.readTree(outputBuilder.toString());
            return parseVideoMetadata(node);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FFMPEGException("Error while getting video metadata: " + e);
        }
    }

    public boolean isMp4File(File file) {
        try {
            String detectedType = tika.detect(file);
            return detectedType.equals("video/mp4");
        } catch (Exception e) {
            return false;
        }
    }

    public void remuxToMp4(File inputFile, File outputFile, ProgressTracker progress, float length) throws IOException, InterruptedException {
        logger.info("Remuxing file {} to {}", inputFile.getAbsolutePath(), outputFile.getAbsolutePath());

        List<String> command = List.of(
                FFMPEG_PATH,
                "-progress", "pipe:1",
                "-i", inputFile.getAbsolutePath(),
                "-c:v", "h264",
                "-c:a", "aac",
                "-f", "mp4",
                outputFile.getAbsolutePath()
        );

        CommandRunner.run(command, line -> setProgress(line, progress, length));

    }

    private VideoMetadata parseVideoMetadata(JsonNode node) {
        VideoMetadata metadata = new VideoMetadata();
        metadata.setStartPoint(0f);

        JsonNode streamNode = node.path("streams").get(0);

        // if stream doesn't exist
        if (streamNode == null || streamNode.isMissingNode()) {
            throw new FFMPEGException("ffprobe streams missing");
        }

        if (streamNode.has("duration")) {
            metadata.setEndPoint(Float.valueOf(streamNode.get("duration").asText()));
        }

        if (streamNode.has("width")) {
            metadata.setWidth(streamNode.get("width").asInt());
        }

        if (streamNode.has("height")) {
            metadata.setHeight(streamNode.get("height").asInt());
        }

        if (streamNode.has("r_frame_rate")) {
            String fpsFraction = streamNode.get("r_frame_rate").asText();

            if (fpsFraction.contains("/")) {
                String[] parts = fpsFraction.split("/");
                double numerator = Float.parseFloat(parts[0]);
                double denominator = Float.parseFloat(parts[1]);
                if (denominator != 0) {
                    metadata.setFps((float) (numerator / denominator));
                }
            } else {
                metadata.setFps(Float.valueOf(fpsFraction)); // Handle cases like "25" directly
            }
        }

        // Extract from the 'format' section
        JsonNode formatNode = node.path("format");
        if (formatNode != null && !formatNode.isMissingNode()) {
            if (formatNode.has("size")) {
                metadata.setFileSize(Float.parseFloat(formatNode.get("size").asText()));
            }

            // Use format duration as a fallback or primary source if stream duration is absent/zero
            if (formatNode.has("duration") && metadata.getEndPoint() == null) {
                metadata.setEndPoint(Float.parseFloat(formatNode.get("duration").asText()));
            }
        }

        return metadata;
    }

    private void setProgress(String line, ProgressTracker progress, float length) {
        Matcher matcher = timePattern.matcher(line);
        if (matcher.find()) {
            float timeInMs = Float.parseFloat(matcher.group(1)) / 1000000f;
            progress.setProgress(timeInMs / length);
        }
    }

    private List<String> buildFilters(Float fps, Integer width, Integer height) {
        List<String> command = new ArrayList<>();
        command.add("-vf");

        List<String> filters = new ArrayList<>();

        if (fps != null) {
            logger.info("Frame rate set to {}", fps);
            filters.add("fps=" + fps);
        }

        if (!(width == null && height == null)) {
            logger.info("Scaling video to width: {}, height: {}", width, height);
            String w = (width != null) ? width.toString() : "-1";
            String h = (height != null) ? height.toString() : "-1";
            filters.add("scale=" + w + ":" + h);
        }

        logger.info("Adding video filters");
        command.add(String.join(",", filters));
        return command;
    }

    private List<String> buildBitrate(Float length, Float fileSize) {
        List<String> command = new ArrayList<>();

        float bitrate = ((fileSize * 8) / length) * BITRATE_MULTIPLIER;
        float audioBitrate = bitrate * AUDIO_RATIO;
        float videoBitrate;

        if (audioBitrate > MAX_AUDIO_BITRATE) {
            audioBitrate = MAX_AUDIO_BITRATE;
            videoBitrate = bitrate - MAX_AUDIO_BITRATE;
        } else {
            videoBitrate = bitrate * (1 - AUDIO_RATIO);
        }

        command.add("-b:v");
        command.add(videoBitrate + "k");
        command.add("-b:a");
        command.add(audioBitrate + "k");

        return command;
    }

    private List<String> buildInputs(File inputFile, Float startPoint, Float length) {
        List<String> command = new ArrayList<>();

        command.add("-ss");
        command.add(startPoint.toString());

        command.add("-i");
        command.add(inputFile.getAbsolutePath());

        command.add("-t");
        command.add(Float.toString(length));

        return command;
    }

    private List<String> buildCommand(File inputFile, File outputFile, VideoMetadata videoMetadata) {
        List<String> command = new ArrayList<>();
        command.add(FFMPEG_PATH);
        command.add("-progress");
        command.add("pipe:1");
        command.add("-y");

        Float length = videoMetadata.getEndPoint() - videoMetadata.getStartPoint();
        command.addAll(buildInputs(inputFile, videoMetadata.getStartPoint(), length));

        if (videoMetadata.getFps() != null || videoMetadata.getWidth() != null || videoMetadata.getHeight() != null) {
            command.addAll(buildFilters(videoMetadata.getFps(), videoMetadata.getWidth(), videoMetadata.getHeight()));
        }

        if (videoMetadata.getFileSize() != null) {
            command.addAll(buildBitrate(length, videoMetadata.getFileSize()));
        }

        // Output file
        command.add(outputFile.getAbsolutePath());
        return command;
    }
}
