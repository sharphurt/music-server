package ru.sharphurt.musicserver.locallibrary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sharphurt.musicserver.locallibrary.enitiy.TrackEntity;

@Repository
public interface TrackRepository extends JpaRepository<TrackEntity, Long> {

    TrackEntity findByiTunesId(long id);
}
