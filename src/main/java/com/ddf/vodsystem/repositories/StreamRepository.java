package com.ddf.vodsystem.repositories;

import com.ddf.vodsystem.entities.Stream;
import com.ddf.vodsystem.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreamRepository extends JpaRepository<Stream, Long> {
    Optional<Stream> findByUserAndEndDateIsNull(User user);
    List<Stream> findByUser(User user);
    List<Stream> findByEndDateIsNullAndLastSeenBefore(Instant cutoff);
}
