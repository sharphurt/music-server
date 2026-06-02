package ru.sharphurt.musicserver.soulseek.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.itunes.service.ITunesSearchService;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekSearchResultDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekSearchTaskDto;
import ru.sharphurt.musicserver.util.DataClearingUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SoulseekSearchService {

    private final SoulseekRestService restService;

    private final ITunesSearchService iTunesSearchService;

    public List<SoulseekSearchTaskDto> initiateSearch(long trackId) {
        TrackDto trackDto = iTunesSearchService.searchTrackById(trackId);

        return trackDto.getTitleAliases().stream()
                .map(alias -> trackDto.getArtistName() + " " + alias)
                .map(DataClearingUtils::normalizeString)
                .map(this::createSearchTask)
                .filter(Objects::nonNull)
                .toList();
    }

    public SoulseekSearchTaskDto createSearchTask(String query) {
        UUID searchId = UUID.randomUUID();
        boolean isSuccessful = restService.postSearchTask(searchId, query);
        if (!isSuccessful) {
            log.info("Задача на поиск для запроса {} не была создана, будет пропущена", query);
            return null;
        }

        return new SoulseekSearchTaskDto(query, searchId);
    }

    public SoulseekSearchResultDto fetchSearchResults(String searchId) {
        Optional<SoulseekSearchResultDto> searchResultDto = restService.getSearchResult(UUID.fromString(searchId));
        if (searchResultDto.isEmpty()) {
            log.info("Не найдено задачи на поиск uuid {}", searchResultDto);
            throw new RuntimeException("Не найдено задачи на поиск uuid " + searchId);
        }

        return searchResultDto.get();
    }
}
