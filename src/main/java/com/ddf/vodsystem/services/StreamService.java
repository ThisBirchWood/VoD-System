package com.ddf.vodsystem.services;

import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.repositories.StreamRepository;

public class StreamService {
    private StreamRepository streamRepository;

    public StreamService(StreamRepository streamRepository) {
        this.streamRepository = streamRepository;
    }

    public void startStream(User user) {

    }

    public void endStream(User user) {

    }

    public void heartbeatStream(User user) {

    }
}
