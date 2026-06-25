package ru.sharphurt.musicserver.mediametadata;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.common.entity.TrackFileStatus;
import ru.sharphurt.musicserver.common.repository.TrackRepository;
import ru.sharphurt.musicserver.mediametadata.itunes.service.ITunesSearchService;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaMetadataService {

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
