package com.ddf.vodsystem.repositories;

import com.ddf.vodsystem.entities.Clip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClipRepository extends JpaRepository<Clip, Long> {
}
