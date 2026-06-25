package com.ddf.vodsystem.services.media;

import com.ddf.vodsystem.dto.CommandOutput;
import com.ddf.vodsystem.dto.ClipOptions;
import com.ddf.vodsystem.exceptions.FFMPEGException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class MetadataService {
    private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);

    private static final String DURATION = "duration";

    @Async("ffmpegTaskExecutor")
    public Future<ClipOptions> getVideoMetadata(Path file) {
        logger.info("Getting metadata for file {}", file);

        List<String> command = List.of(
                "ffprobe",
                "-v", "quiet",
                "-print_format", "json",
                "-show_format", "-select_streams",
                "v:0", "-show_entries", "stream=duration,width,height,r_frame_rate:format=size,duration",
                "-i", file.toAbsolutePath().toString()
        );

        ObjectMapper mapper = new ObjectMapper();
        StringBuilder outputBuilder = new StringBuilder();

        try {
            CommandOutput output = CommandRunner.run(command);

            for (String line : output.getOutput()) {
                outputBuilder.append(line);
            }

            JsonNode node = mapper.readTree(outputBuilder.toString());
            return CompletableFuture.completedFuture(parseVideoMetadata(node));
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FFMPEGException("Error while getting video metadata: " + e);
        }
    }

    private ClipOptions parseVideoMetadata(JsonNode node) {
        ClipOptions metadata = new ClipOptions();
        metadata.setStartPoint(0f);

        JsonNode streamNode = extractStreamNode(node);

        metadata.setDuration(extractDuration(streamNode));
        metadata.setWidth(getWidth(streamNode));
        metadata.setHeight(getHeight(streamNode));
        metadata.setFps(extractFps(streamNode));

        JsonNode formatNode = extractFormatNode(node);
        metadata.setFileSize(extractFileSize(formatNode));
        extractEndPointFromFormat(metadata, formatNode);

        return metadata;
    }

    private JsonNode extractStreamNode(JsonNode node) {
        JsonNode streamNode = node.path("streams").get(0);
        if (streamNode == null || streamNode.isMissingNode()) {
            throw new FFMPEGException("ffprobe streams missing");
        }
        return streamNode;
    }

    private Float extractDuration(JsonNode streamNode) {
        if (streamNode.has(DURATION)) {
            return Float.valueOf(streamNode.get(DURATION).asText());
        }

        throw new FFMPEGException("ffprobe duration missing");
    }

    private Integer getWidth(JsonNode streamNode) {
        if (streamNode.has("width")) {
            return streamNode.get("width").asInt();
        }

        throw new FFMPEGException("ffprobe width missing");
    }

    private Integer getHeight(JsonNode streamNode) {
        if (streamNode.has("height")) {
            return streamNode.get("height").asInt();
        }

        throw new FFMPEGException("ffprobe height missing");
    }

    private JsonNode extractFormatNode(JsonNode node) {
        JsonNode formatNode = node.path("format");
        return (formatNode != null && !formatNode.isMissingNode()) ? formatNode : null;
    }

    private Float extractFileSize(JsonNode formatNode) {
        if (formatNode != null && formatNode.has("size")) {
            return Float.parseFloat(formatNode.get("size").asText());
        }

        throw new FFMPEGException("ffprobe file size missing");
    }

    private void extractEndPointFromFormat(ClipOptions metadata, JsonNode formatNode) {
        if (formatNode != null && formatNode.has(DURATION) && metadata.getDuration() == null) {
            metadata.setDuration(Float.parseFloat(formatNode.get(DURATION).asText()));
        }
    }

    private Float extractFps(JsonNode streamNode) {
        if (streamNode.has("r_frame_rate")) {
            String fpsFraction = streamNode.get("r_frame_rate").asText();

            if (fpsFraction.contains("/")) {
                String[] parts = fpsFraction.split("/");
                double numerator = Float.parseFloat(parts[0]);
                double denominator = Float.parseFloat(parts[1]);
                if (denominator != 0) {
                    return (float) (numerator / denominator);
                }
            } else {
                return Float.valueOf(fpsFraction); // Handle cases like "25" directly
            }
        }
        return null;
    }
}