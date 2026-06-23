package ru.sharphurt.musicserver.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sharphurt.musicserver.library.enitiy.TrackEntity;

@Repository
public interface TrackRepository extends JpaRepository<TrackEntity, Long> {

    TrackEntity findByiTunesId(long id);
}
