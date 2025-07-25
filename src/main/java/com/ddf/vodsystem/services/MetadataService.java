package com.ddf.vodsystem.services;

import com.ddf.vodsystem.dto.CommandOutput;
import com.ddf.vodsystem.dto.VideoMetadata;
import com.ddf.vodsystem.exceptions.FFMPEGException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class MetadataService {
    private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);

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

    public Float getFileSize(File file) {
        logger.info("Getting file size for {}", file.getAbsolutePath());
        VideoMetadata metadata = getVideoMetadata(file);

        if (metadata.getFileSize() == null) {
            throw new FFMPEGException("File size not found");
        }

        return metadata.getFileSize();
    }

    public void normalizeVideoMetadata(VideoMetadata inputFileMetadata, VideoMetadata outputFileMetadata) {
        if (outputFileMetadata.getStartPoint() == null) {
            outputFileMetadata.setStartPoint(0f);
        }

        if (outputFileMetadata.getEndPoint() == null) {
            outputFileMetadata.setEndPoint(inputFileMetadata.getEndPoint());
        }
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

}
