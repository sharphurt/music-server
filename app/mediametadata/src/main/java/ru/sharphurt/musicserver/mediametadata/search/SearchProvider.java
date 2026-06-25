package ru.sharphurt.musicserver.mediametadata.search;


import ru.sharphurt.musicserver.common.entity.AlbumEntity;
import ru.sharphurt.musicserver.common.entity.TrackEntity;

public interface SearchProvider {

    SearchResult<TrackEntity> searchTracksBy(String query, long limit, long page);

    TrackEntity searchTrackById(long id);

    AlbumEntity searchAlbumById(long id);
}
