package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.VideoMetadata;
import com.ddf.vodsystem.exceptions.FFMPEGException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class MetadataService {
    private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);

    public VideoMetadata getVideoMetadata(File file) {
        logger.info("Getting metadata for file {}", file.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder("ffprobe",
                "-v", "quiet",
                "-print_format", "json",
                "-show_format", "-select_streams",
                "v:0", "-show_entries", "stream=duration,width,height,r_frame_rate:format=size,duration",
                "-i", file.getAbsolutePath());

        Process process;

        try {
           process = pb.start();
           handleFfprobeError(process);
           VideoMetadata metadata = parseVideoMetadata(readStandardOutput(process));
           logger.info("Metadata for file {} finished with exit code {}", file.getAbsolutePath(), process.exitValue());
           return metadata;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FFMPEGException(e.getMessage());
        } catch (IOException e) {
            throw new FFMPEGException(e.getMessage());
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

    public Float getVideoDuration(File file) {
        logger.info("Getting video duration for {}", file.getAbsolutePath());
        VideoMetadata metadata = getVideoMetadata(file);
        if (metadata.getEndPoint() == null) {
            throw new FFMPEGException("Video duration not found");
        }
        return metadata.getEndPoint();
    }

    public void normalizeVideoMetadata(VideoMetadata inputFileMetadata, VideoMetadata outputFileMetadata) {
        if (outputFileMetadata.getStartPoint() == null) {
            outputFileMetadata.setStartPoint(0f);
        }

        if (outputFileMetadata.getEndPoint() == null) {
            outputFileMetadata.setEndPoint(inputFileMetadata.getEndPoint());
        }
    }

    private JsonNode readStandardOutput(Process process) throws IOException{
        // Read the standard output (JSON metadata)
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder jsonOutput = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonOutput.append(line);
        }

        // Parse the JSON output
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(jsonOutput.toString());
    }

    private void handleFfprobeError(Process process) throws IOException, InterruptedException {
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorOutput = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            throw new FFMPEGException("ffprobe exited with code " + exitCode + ". Error: " + errorOutput);
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
