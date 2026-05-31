package ru.sharphurt.musicserver.search.enrichment;

import de.umass.lastfm.Track;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.search.dto.TrackDto;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class LastFmEnrichmentService implements EnrichmentService<TrackDto> {

    @Value("${lastfm.apikey}")
    private String lastFmApiKey;

    public void enrich(TrackDto dto) {
        try {
            String trackIdentifier = Objects.requireNonNullElse(dto.getMbid(), dto.getTitle());
            Track trackInfo = Track.getInfo(dto.getArtistName(), trackIdentifier, lastFmApiKey);

            if (trackInfo != null) {
                dto.setPlaycounts(trackInfo.getPlaycount());
            }
        } catch (Exception e) {
            log.warn("Не удалось получить данные Last.fm для: {} - {}. Ошибка: {}",
                    dto.getArtistName(), dto.getTitle(), e.getMessage());
            dto.setPlaycounts(0L);
        }
    }
}