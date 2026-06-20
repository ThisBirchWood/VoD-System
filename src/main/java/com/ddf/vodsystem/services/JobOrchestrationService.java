package com.ddf.vodsystem.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.ddf.vodsystem.dto.Job;
import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.exceptions.StorageException;
import com.ddf.vodsystem.services.media.CompressionService;
import com.ddf.vodsystem.services.media.RemuxService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the processing pipeline for a {@link Job}: converting (remuxing)
 * the input file and, separately, compressing/persisting the output clip.
 * <p>
 * Job storage and lookup are handled by {@link JobRegistryService}; this class
 * is only responsible for acting on a {@link Job} instance once it's been retrieved.
 * Long-running steps (remux, compress) are kicked off asynchronously via
 * {@link java.util.concurrent.CompletableFuture}-returning service calls rather
 * than blocked on, so callers should not assume the job is finished when these
 * methods return.
 */
@Service
public class JobOrchestrationService {
    private static final Logger logger = LoggerFactory.getLogger(JobOrchestrationService.class);
    private final ClipService clipService;
    private final RemuxService remuxService;
    private final DirectoryService directoryService;
    private final CompressionService compressionService;
    private final UserService userService;

    /**
     * Constructs the orchestration service with its collaborating services.
     *
     * @param clipService         used to persist a completed clip's metadata once processing succeeds
     * @param remuxService        performs the remux step during conversion
     * @param compressionService  performs the compression step during processing
     * @param directoryService    used for filesystem operations (e.g. copying the input file to a temp location)
     * @param userService         used to resolve the currently logged-in user when persisting a clip
     */
    public JobOrchestrationService(ClipService clipService,
                                   RemuxService remuxService,
                                   CompressionService compressionService,
                                   DirectoryService directoryService,
                                   UserService userService) {
        this.clipService = clipService;
        this.remuxService = remuxService;
        this.directoryService = directoryService;
        this.compressionService = compressionService;
        this.userService = userService;
    }

    /**
     * Converts a job's input file in place by remuxing it.
     * <p>
     * The original input file is first copied to a temporary {@code .temp} file so the
     * remux can read from a stable source; on successful completion the temp file is
     * deleted. The remux itself runs asynchronously — this method returns once the
     * remux has been kicked off, not once it has finished. Progress/completion is
     * tracked via {@code job.getStatus().getConversion()}, which is reset at the
     * start of this call and marked complete in the remux's completion callback.
     *
     * @param job the job whose input file should be converted
     * @throws StorageException if the temporary file cannot be created, or if it
     *         cannot be deleted after a successful remux
     */
    public void convertJob(Job job) {
        logger.info("Converting job: {}", job.getUuid());
        File tempFile = new File(job.getInputFile().getAbsolutePath() + ".temp");

        try {
            directoryService.copyFile(job.getInputFile(), tempFile);
        } catch (IOException e) {
            throw new StorageException("Could not create temporary file: " + tempFile, e);
        }

        job.getStatus().getConversion().reset();

        try {
            remuxService.remux(
                    tempFile,
                    job.getInputFile(),
                    job.getStatus().getConversion(),
                    job.getInputClipOptions().getDuration())
                    .thenRun(() -> {
                        job.getStatus().getConversion().markComplete();

                        try {
                            Files.deleteIfExists(Paths.get(tempFile.getAbsolutePath()));
                        } catch (IOException e) {
                            throw new StorageException("Could not delete path at: " + tempFile.getAbsolutePath(), e);
                        }
                    }).whenComplete((ignored, throwable) -> {
                        if (throwable != null) {
                            logger.error("Remux failed for jobId={}", job.getUuid(), throwable);
                        } else {
                            logger.info("Remux completed for jobId={}", job.getUuid());
                        }
                    });

        } catch (IOException | InterruptedException e) {
            logger.error("Error converting job {}: {}", job.getUuid(), e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Compresses a job's output file according to its output clip options and, on
     * success, persists the resulting clip against the currently logged-in user (if any).
     * <p>
     * Compression runs asynchronously — this method returns once compression has been
     * kicked off, not once it has finished. Progress is tracked via
     * {@code job.getStatus().getProcess()}, which is reset at the start of this call.
     * If compression or persistence fails, the job's status is marked failed with the
     * triggering exception's message.
     *
     * @param job the job to process
     */
    public void processJob(Job job) {
        logger.info("Job ready: {}", job.getUuid());
        job.getStatus().getProcess().reset();

        try {
            Optional<User> user = userService.getLoggedInUser();
            compressionService.compress(job.getInputFile(), job.getOutputFile(), job.getOutputClipOptions(), job.getStatus().getProcess())
                    .thenRun(() -> user.ifPresent(userVal ->
                            clipService.persistClip(
                                    job.getOutputClipOptions(),
                                    userVal,
                                    job.getOutputFile(),
                                    job.getInputFile().getName()
                            )
                    )).exceptionally(
                            ex -> {
                                job.getStatus().setFailed(true);
                                job.getStatus().setFailedReason(ex.getMessage());
                                logger.error("Error processing job {}: {}", job.getUuid(), ex.getMessage());
                                return null;
                            }
                    );
        } catch (IOException | InterruptedException e) {
            job.getStatus().setFailed(true);
            job.getStatus().setFailedReason(e.getMessage());
            logger.error("Error processing job {}: {}", job.getUuid(), e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}