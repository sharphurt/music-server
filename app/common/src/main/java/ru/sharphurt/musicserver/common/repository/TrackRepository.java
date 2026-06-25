package ru.sharphurt.musicserver.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sharphurt.musicserver.common.entity.TrackEntity;

@Repository
public interface TrackRepository extends JpaRepository<TrackEntity, Long> {

    TrackEntity findByiTunesId(long id);
}
