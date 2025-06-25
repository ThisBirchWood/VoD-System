package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.*;
import com.ddf.vodsystem.repositories.ClipRepository;
import com.ddf.vodsystem.security.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EditService {
    private final JobService jobService;
    private final ClipRepository clipRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EditService.class);

    public EditService(JobService jobService, ClipRepository clipRepository) {
        this.jobService = jobService;
        this.clipRepository = clipRepository;
    }

    public void edit(String uuid, VideoMetadata videoMetadata) {
        Job job = jobService.getJob(uuid);
        validateClipConfig(videoMetadata);
        job.setOutputVideoMetadata(videoMetadata);
    }

    public void process(String uuid) {
        jobService.jobReady(uuid);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomOAuth2User oAuth2user) {
            logger.debug("Saving clip {} for user {}", uuid, oAuth2user.getName());
            VideoMetadata videoMetadata = jobService.getJob(uuid).getOutputVideoMetadata();
            User user = oAuth2user.getUser();

            createClip(videoMetadata, user);
        }
    }

    public float getProgress(String uuid) {
        Job job = jobService.getJob(uuid);

        if (job.getStatus() == JobStatus.FINISHED) {
            return 1f;
        }

        return job.getProgress();
    }

    private void validateClipConfig(VideoMetadata videoMetadata) {
        Float start = videoMetadata.getStartPoint();
        Float end = videoMetadata.getEndPoint();
        Float fileSize = videoMetadata.getFileSize();
        Integer width = videoMetadata.getWidth();
        Integer height = videoMetadata.getHeight();
        Float fps = videoMetadata.getFps();

        if (start != null && start < 0) {
            throw new IllegalArgumentException("Start point cannot be negative");
        }

        if (end != null && end < 0) {
            throw new IllegalArgumentException("End point cannot be negative");
        }

        if (start != null && end != null && end <= start) {
            throw new IllegalArgumentException("End point must be greater than start point");
        }

        if (fileSize != null && fileSize < 100) {
            throw new IllegalArgumentException("File size cannot be less than 100kb");
        }

        if (width != null && width < 1) {
            throw new IllegalArgumentException("Width cannot be less than 1");
        }

        if (height != null && height < 1) {
            throw new IllegalArgumentException("Height cannot be less than 1");
        }

        if (fps != null && fps < 1) {
            throw new IllegalArgumentException("FPS cannot be less than 1");
        }
    }

    private void createClip(VideoMetadata videoMetadata, User user) {
        Clip clip = new Clip();
        clip.setTitle("test");
        clip.setUser(user);
        clip.setDescription("This is a test");
        clip.setCreatedAt(LocalDateTime.now());
        clip.setWidth(videoMetadata.getWidth());
        clip.setHeight(videoMetadata.getHeight());
        clip.setFps(videoMetadata.getFps());
        clip.setDuration(videoMetadata.getEndPoint() - videoMetadata.getStartPoint());
        clip.setFileSize(videoMetadata.getFileSize());
        clip.setVideoPath("test");
        clipRepository.save(clip);
    }
}
