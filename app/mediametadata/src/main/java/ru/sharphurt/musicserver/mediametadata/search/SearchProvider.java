package ru.sharphurt.musicserver.mediametadata.search;


import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import ru.sharphurt.musicserver.common.entity.AlbumEntity;
import ru.sharphurt.musicserver.common.entity.ArtistEntity;
import ru.sharphurt.musicserver.common.entity.TrackEntity;

public interface SearchProvider {

    SearchResult<TrackEntity> searchTracksBy(String query, long limit, long page);

    TrackEntity searchTrackById(long id);

    Pair<AlbumEntity, List<TrackEntity>> searchAlbumWithTracksById(long id);

    ArtistEntity searchArtistById(long id);
}
