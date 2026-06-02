package ru.sharphurt.musicserver.dataenrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.async.AsyncExecutor;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.itunes.dto.ITunesTrackDto;
import ru.sharphurt.musicserver.itunes.service.ITunesRestService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AliasesEnrichmentService implements EnrichmentService<TrackDto> {

    public static final List<String> ALIAS_LOCALES = List.of("JP");

    private final AsyncExecutor executor;
    private final ITunesRestService iTunesRestService;

    public TrackDto enrich(TrackDto dto) {
        Set<String> trackNameAliases = new HashSet<>();
        Set<String> artistNameAliases = new HashSet<>();


        log.info("Creating aliases for track {}", dto);

        executor.callForMultipleArgumentsAsync(ALIAS_LOCALES, c -> findTrackInCountry(dto, c)).stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(t -> {
                    trackNameAliases.add(t.trackName().toLowerCase());
                    artistNameAliases.add(t.artistName().toLowerCase());
                });

        return dto.addArtistAliases(artistNameAliases)
                .addTitleAliases(trackNameAliases);
    }

    private Optional<ITunesTrackDto> findTrackInCountry(TrackDto trackDto, String country) {
        log.info("Поиск трека id={} в стране: {}", trackDto, country);
        return iTunesRestService.lookup(trackDto, country);
    }
}