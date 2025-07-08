package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.VideoMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class FfmpegService {
    private static final Logger logger = LoggerFactory.getLogger(FfmpegService.class);

    private static final float AUDIO_RATIO = 0.15f;
    private static final float MAX_AUDIO_BITRATE = 128f;
    private static final float BITRATE_MULTIPLIER = 0.9f;

    private void buildFilters(ArrayList<String> command, Float fps, Integer width, Integer height) {
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

        if (!filters.isEmpty()) {
            logger.info("Adding video filters");
            command.add("-vf");
            command.add(String.join(",", filters));
        }
    }

    private void buildBitrate(ArrayList<String> command, Float length, Float fileSize) {
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
    }

    private void buildInputs(ArrayList<String> command, File inputFile, Float startPoint, Float length) {
        command.add("-ss");
        command.add(startPoint.toString());

        command.add("-i");
        command.add(inputFile.getAbsolutePath());

        command.add("-t");
        command.add(Float.toString(length));
    }

    public ProcessBuilder buildCommand(File inputFile, File outputFile, VideoMetadata videoMetadata) {
        ArrayList<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-progress");
        command.add("pipe:1");
        command.add("-y");

        Float length = videoMetadata.getEndPoint() - videoMetadata.getStartPoint();
        buildInputs(command, inputFile, videoMetadata.getStartPoint(), length);
        buildFilters(command, videoMetadata.getFps(), videoMetadata.getWidth(), videoMetadata.getHeight());

        if (videoMetadata.getFileSize() != null) {
            buildBitrate(command, length, videoMetadata.getFileSize());
        }

        // Output file
        command.add(outputFile.getAbsolutePath());
        return new ProcessBuilder(command);
    }
}
