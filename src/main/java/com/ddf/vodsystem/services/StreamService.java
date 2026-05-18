package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.Stream;
import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.exceptions.AlreadyStreaming;
import com.ddf.vodsystem.exceptions.KeyNotFound;
import com.ddf.vodsystem.repositories.StreamRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StreamService {
    private final StreamRepository streamRepository;
    private final UserService userService;

    public StreamService(StreamRepository streamRepository, UserService userService) {
        this.streamRepository = streamRepository;
        this.userService = userService;
    }

    public Stream startStream(String streamKey) {
        User user = resolveUser(streamKey);

        streamRepository.findByUserAndEndDateIsNull(user).ifPresent(existing -> {
            throw new AlreadyStreaming("User " + user.getUsername() + " is already streaming.");
        });

        LocalDateTime now = LocalDateTime.now();
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


        stream.setEndDate(LocalDateTime.now());
        streamRepository.saveAndFlush(stream);
    }

    public void heartbeatStream(String streamKey) {
        User user = resolveUser(streamKey);

        Stream stream = streamRepository.findByUserAndEndDateIsNull(user)
                .orElseThrow(() -> new IllegalStateException(
                        "No active stream found for user " + user.getUsername()));

        stream.setLastSeen(LocalDateTime.now());
        streamRepository.saveAndFlush(stream);
    }

    public Optional<Stream> getActiveStream(String streamKey) {
        User user = resolveUser(streamKey);
        return streamRepository.findByUserAndEndDateIsNull(user);
    }

    public List<Stream> getStreamHistory(Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return streamRepository.findByUser(user);
    }

    private User resolveUser(String streamKey) {
        return userService.getUserByStreamKey(streamKey)
                .orElseThrow(() -> new KeyNotFound("Stream key not found: " + streamKey));
    }
}
