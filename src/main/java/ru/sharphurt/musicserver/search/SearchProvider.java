package ru.sharphurt.musicserver.search;

import ru.sharphurt.musicserver.search.dto.RawSearchResultDto;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;
import ru.sharphurt.musicserver.search.dto.TrackDto;

public interface SearchProvider {
    RawSearchResultDto<TrackDto> searchTracks(SearchRequestDto request);
}
