package ru.sharphurt.musicserver.search;

import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;
import ru.sharphurt.musicserver.search.dto.SearchResponseDto;

public interface SearchProvider {
    SearchResponseDto<TrackDto> searchTracksBy(SearchRequestDto request);

    TrackDto searchTrackById(long id);
}
