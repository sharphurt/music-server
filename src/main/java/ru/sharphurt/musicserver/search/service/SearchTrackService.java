package ru.sharphurt.musicserver.search.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.library.enitiy.TrackEntity;
import ru.sharphurt.musicserver.search.SearchProvider;
import ru.sharphurt.musicserver.search.dto.SearchRequestDto;
import ru.sharphurt.musicserver.search.dto.SearchResponseDto;

@Service
public class SearchTrackService {

    private final SearchProvider searchProvider;

    public SearchTrackService(@Qualifier("ITunesSearchService") SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    public SearchResponseDto<TrackEntity> searchTracks(@NonNull SearchRequestDto request) {
        return searchProvider.searchTracksBy(request);
    }
}
