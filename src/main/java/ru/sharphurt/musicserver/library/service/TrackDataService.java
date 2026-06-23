package ru.sharphurt.musicserver.library.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.itunes.service.ITunesSearchService;
import ru.sharphurt.musicserver.library.enitiy.TrackEntity;
import ru.sharphurt.musicserver.library.enitiy.TrackFileStatus;
import ru.sharphurt.musicserver.library.repository.TrackRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackDataService {

    private final TrackRepository trackRepository;
    private final ITunesSearchService iTunesSearchService;

    @Transactional
    public TrackEntity getTrackData(long trackId) {
        TrackEntity track = trackRepository.findByiTunesId(trackId);
        if (track != null) {
            return track;
        }

        log.info("Для запрошенного трека {} не найдено данных. Загружаем из ITunes", trackId);
        track = iTunesSearchService.searchTrackById(trackId);

        if (track != null) {
            track.setTrackStatus(TrackFileStatus.NOT_DOWNLOADED);
            return trackRepository.save(track);
        }

        log.error("Трека с id {} не найдено", trackId);
        return null;
    }
}
