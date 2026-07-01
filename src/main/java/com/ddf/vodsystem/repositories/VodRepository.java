package com.ddf.vodsystem.repositories;

import com.ddf.vodsystem.entities.User;
import com.ddf.vodsystem.entities.Vod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VodRepository extends JpaRepository<Vod, Long>
{
    @Query("SELECT v FROM Vod v WHERE v.user = ?1")
    List<Vod> findByUser(User user);
}
