package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.Marker;
import com.ddf.vodsystem.entities.Stream;
import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.exceptions.MarkerNotFound;
import com.ddf.vodsystem.exceptions.NotAuthenticated;
import com.ddf.vodsystem.exceptions.NotStreaming;
import com.ddf.vodsystem.repositories.MarkerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class MarkerService {

    private final UserService userService;
    private final StreamService streamService;
    private final MarkerRepository markerRepository;

    @Value("${stream.max-length}")
    private Long maxStreamLength;

    public MarkerService(UserService userService, StreamService streamService, MarkerRepository markerRepository) {
        this.userService = userService;
        this.streamService = streamService;
        this.markerRepository = markerRepository;
    }

    public Marker getMarkerById(Long id) {
        Optional<Marker> marker = markerRepository.findById(id);

        if (marker.isEmpty()) {
            throw new MarkerNotFound("Marker does not exist");
        }

        Optional<User> user = userService.getLoggedInUser();
        if (user.isEmpty() || !user.get().equals(marker.get().getUser())) {
            throw new NotAuthenticated("User not authenticated for this marker");
        }

        return marker.get();
    }

    public List<Marker> getUserMarkers() {
        Optional<User> user = userService.getLoggedInUser();

        if (user.isEmpty()) {
            throw new NotAuthenticated("Must be logged in to get markers");
        }

        return markerRepository.findByUser(user.get());
    }

    public Marker create(String message) {
        Optional<Stream> stream = streamService.getActiveStream();

        if (stream.isEmpty()) {
            throw new NotStreaming("User must be streaming to mark");
        }

        Marker marker = new Marker();
        marker.setStream(stream.get());
        marker.setUser(stream.get().getUser());
        marker.setMessage(message);
        marker.setTimestamp(Instant.now());
        return markerRepository.saveAndFlush(marker);
    }

    public void deleteMarker(Long id) {
        Marker marker = getMarkerById(id);
        markerRepository.delete(marker);
    }

    @Scheduled(fixedDelay = 360_000)
    @Transactional
    public void deleteOldMarkers() {
        Instant cutoff = Instant.now().minusSeconds(maxStreamLength);
        List<Marker> stale = markerRepository.findAllBefore(cutoff);
        markerRepository.deleteAllInBatch(stale);
    }
}
