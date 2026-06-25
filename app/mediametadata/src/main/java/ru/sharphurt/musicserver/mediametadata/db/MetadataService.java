package ru.sharphurt.musicserver.mediametadata.db;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.common.entity.AlbumEntity;
import ru.sharphurt.musicserver.common.entity.ArtistEntity;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.common.repository.AlbumRepository;
import ru.sharphurt.musicserver.common.repository.ArtistRepository;
import ru.sharphurt.musicserver.common.repository.TrackRepository;
import ru.sharphurt.musicserver.mediametadata.exception.AlbumNotFoundException;
import ru.sharphurt.musicserver.mediametadata.exception.ArtistNotFoundException;
import ru.sharphurt.musicserver.mediametadata.exception.TrackNotFoundException;
import ru.sharphurt.musicserver.mediametadata.search.SearchProvider;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MetadataService {

    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final SearchProvider searchProvider;

    public ArtistEntity findOrFetchArtist(long artistId) {
        return artistRepository.findById(artistId)
            .orElseGet(() -> {
                ArtistEntity artist = searchProvider.searchArtistById(artistId);
                if (artist == null) {
                    log.warn("Неизвестный артист artistId={}", artistId);
                    throw new ArtistNotFoundException(artistId);
                }
                artistRepository.upsert(artist);
                return artist;
            });
    }

    public AlbumEntity findOrFetchAlbum(long albumId) {
        return albumRepository.findById(albumId)
            .orElseGet(() -> {
                Pair<AlbumEntity, List<TrackEntity>> result = searchProvider.searchAlbumWithTracksById(albumId);
                if (result == null || result.getLeft() == null) {
                    log.warn("Неизвестный альбом albumId={}", albumId);
                    throw new AlbumNotFoundException(albumId);
                }

                AlbumEntity album = result.getLeft();
                ArtistEntity artist = findOrFetchArtist(album.getArtistId());
                album.setArtist(artist);
                albumRepository.upsert(album);

                result.getRight().stream()
                    .filter(t -> !trackRepository.existsById(t.getITunesId()))
                    .forEach(t -> {
                        t.setAlbum(album);
                        t.setArtist(artist);
                        trackRepository.saveAndFlush(t);
                    });

                return album;
            });
    }

    public TrackEntity findOrFetchTrack(long trackId) {
        return trackRepository.findById(trackId)
            .orElseGet(() -> {
                TrackEntity result = searchProvider.searchTrackById(trackId);
                if (result == null) {
                    log.warn("Неизвестный трек trackId={}", trackId);
                    throw new TrackNotFoundException(trackId);
                }

                AlbumEntity album = findOrFetchAlbum(result.getAlbumId());

                return trackRepository.findById(trackId)
                    .orElseGet(() -> {
                        result.setAlbum(album);
                        result.setArtist(album.getArtist());
                        return trackRepository.saveAndFlush(result);
                    });
            });
    }
}