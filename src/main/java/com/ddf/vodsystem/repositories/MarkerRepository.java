package com.ddf.vodsystem.repositories;

import com.ddf.vodsystem.entities.Marker;
import com.ddf.vodsystem.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MarkerRepository extends JpaRepository<Marker, Long> {
    @Query("SELECT m FROM Marker m WHERE m.user = ?1")
    List<Marker> findByUser(User user);

    @Query("SELECT m FROM Marker m WHERE m.timestamp <= ?1")
    List<Marker> findAllBefore(Instant time);
}
