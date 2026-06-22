package ru.sharphurt.musicserver.search;

import ru.sharphurt.musicserver.locallibrary.enitiy.TrackEntity;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;
import ru.sharphurt.musicserver.search.dto.SearchResponseDto;

public interface SearchProvider {
    SearchResponseDto<TrackEntity> searchTracksBy(SearchRequestDto request);

    TrackEntity searchTrackById(long id);
}
