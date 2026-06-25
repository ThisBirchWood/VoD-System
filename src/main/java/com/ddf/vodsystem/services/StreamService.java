package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.Stream;
import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.exceptions.AlreadyStreaming;
import com.ddf.vodsystem.exceptions.KeyNotFound;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.repositories.StreamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class StreamService {
    private static final Logger logger = LoggerFactory.getLogger(StreamService.class);

    private static final int HEARTBEAT_TIMEOUT_SECONDS = 60;

    private final StreamRepository streamRepository;
    private final UserService userService;

    public StreamService(StreamRepository streamRepository,
                         UserService userService) {
        this.streamRepository = streamRepository;
        this.userService = userService;
    }

    public Stream startStream(String streamKey) {
        User user = resolveUser(streamKey);

        streamRepository.findByUserAndEndDateIsNull(user).ifPresent(existing -> {
            throw new AlreadyStreaming("User " + user.getUsername() + " is already streaming.");
        });

        Instant now = Instant.now();
        Stream stream = new Stream();
        stream.setUser(user);
        stream.setStartDate(now);
        stream.setLastSeen(now);
        return streamRepository.saveAndFlush(stream);
    }

    public void endStream(String streamKey) {
        User user = resolveUser(streamKey);

        Stream stream = streamRepository.findByUserAndEndDateIsNull(user)
                .orElseThrow(() -> new IllegalStateException(
                        "No active stream found for user " + user.getUsername()));

        stream.setEndDate(Instant.now());
        streamRepository.saveAndFlush(stream);
    }

    public void heartbeatStream(String streamKey) {
        User user = resolveUser(streamKey);

        Stream stream = streamRepository.findByUserAndEndDateIsNull(user)
                .orElseThrow(() -> new IllegalStateException(
                        "No active stream found for user " + user.getUsername()));

        stream.setLastSeen(Instant.now());
        streamRepository.saveAndFlush(stream);
    }

    public Optional<Stream> getActiveStream() {
        Optional<User> user = userService.getLoggedInUser();

        if (user.isEmpty()) {
            throw new NotAuthenticated("Log in to see user streams");
        }

        return streamRepository.findByUserAndEndDateIsNull(user.get());
    }

    public List<Stream> getStreamHistory(Long userId) {
        User streamUser = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Optional<User> user = userService.getLoggedInUser();

        if (user.isEmpty()) {
            throw new NotAuthenticated("Log in to see user streams");
        }

        if (!user.get().equals(streamUser)) {
            throw new NotAuthenticated("You are not authenticated to see these user streams");
        }

        return streamRepository.findByUser(streamUser);
    }

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void endStaleStreams() {
        Instant cutoff = Instant.now().minusSeconds(HEARTBEAT_TIMEOUT_SECONDS);
        List<Stream> stale = streamRepository.findByEndDateIsNullAndLastSeenBefore(cutoff);
        if (stale.isEmpty()) return;

        Instant now = Instant.now();
        for (Stream stream : stale) {
            stream.setEndDate(now);
            logger.warn("Stream {} for user {} ended due to heartbeat timeout", stream.getId(), stream.getUser().getUsername());
        }
        streamRepository.saveAllAndFlush(stale);
    }

    private User resolveUser(String streamKey) {
        return userService.getUserByStreamKey(streamKey)
                .orElseThrow(() -> new KeyNotFound("Stream key not found: " + streamKey));
    }
}
