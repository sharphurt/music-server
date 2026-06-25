package ru.sharphurt.musicserver.mediametadata.dataenrichment;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.common.async.AsyncExecutor;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.mediametadata.itunes.rest.ITunesLookupRestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AliasesEnrichmentService implements EnrichmentService<TrackEntity> {

    public static final List<String> ALIAS_LOCALES = List.of("JP");

    private final AsyncExecutor executor;

    private final ITunesLookupRestClient lookupRestClient;

    public TrackEntity enrich(TrackEntity dto) {
        Set<String> trackNameAliases = new HashSet<>();
        Set<String> artistNameAliases = new HashSet<>();

        log.info("Creating aliases for track {}", dto);

        executor.callForMultipleArgumentsAsync(ALIAS_LOCALES, country -> lookupRestClient.getTrack(dto.getITunesId(), country))
            .stream()
            .filter(Objects::nonNull)
            .forEach(t -> {
                trackNameAliases.add(t.getTrackName().toLowerCase());
                artistNameAliases.add(t.getArtistName().toLowerCase());
            });

        return dto.addArtistAliases(artistNameAliases)
            .addTitleAliases(trackNameAliases);
    }

}