package ru.sharphurt.musicserver.soulseek.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.itunes.service.ITunesSearchService;
import ru.sharphurt.musicserver.redis.SoulseekTaskCacheService;
import ru.sharphurt.musicserver.soulseek.dto.*;
import ru.sharphurt.musicserver.util.DataClearingUtils;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SoulseekSearchService {

    private final SoulseekRestService restService;
    private final ITunesSearchService iTunesSearchService;
    private final SoulseekTaskCacheService cacheService;
    private final SoulseekScoringService scoringService;

    public List<SoulseekSearchTaskDto> initiateSearch(long trackId) {
        TrackDto trackDto = iTunesSearchService.searchTrackById(trackId);
        List<SoulseekSearchTaskDto> searchTasks = trackDto.getTitleAliases().stream().flatMap(alias -> Stream.of(trackDto.getArtistName() + " " + alias, alias + " " + trackDto.getArtistName(), alias)).map(e -> DataClearingUtils.normalizeString(e).toLowerCase()).filter(e -> !e.isEmpty()).distinct().map(query -> createSearchTask(trackDto, query)).filter(Objects::nonNull).toList();

        for (SoulseekSearchTaskDto searchTask : searchTasks) {
            cacheService.save(searchTask);
        }

        return searchTasks;
    }

    public SoulseekSearchTaskDto createSearchTask(TrackDto trackDto, String query) {
        UUID searchId = UUID.randomUUID();
        boolean isSuccessful = restService.postSearchTask(searchId, query);
        if (!isSuccessful) {
            log.info("Задача на поиск для запроса {} не была создана, будет пропущена", query);
            return null;
        }

        return new SoulseekSearchTaskDto(trackDto.getITunesId(), query, searchId);
    }

    public List<SoulseekFileScoreDto> fetchSearchResults(long trackId, int maxResults) {
        TrackDto trackDto = iTunesSearchService.searchTrackById(trackId);
        List<SoulseekFileNodeDto> filesData = collectFiles(trackId);

        return scoringService.matchAndSort(trackDto, filesData).stream().limit(maxResults).toList();
    }

    private List<SoulseekFileNodeDto> collectFiles(long trackId) {
        List<SoulseekSearchTaskDto> tasksData = cacheService.getAllForTrack(trackId);

        if (tasksData == null || tasksData.isEmpty()) {
            log.info("Для трека id={} нет активных задач на поиск", trackId);
            return List.of();
        }

        Map<String, SoulseekFileNodeDto> aggregatedFiles = new HashMap<>();

        for (SoulseekSearchTaskDto task : tasksData) {
            try {
                Optional<SoulseekSearchResultDto> searchResult = restService.getSearchResult(task.uuid());

                if (searchResult.isEmpty()) {
                    log.error("Для задачи {} Soulseek ничего не вернул. Пропускаем", task);
                    cacheService.delete(task);
                    continue;
                }

                for (SoulseekPeerResponseDto peerResponse : searchResult.get().getResponses()) {
                    if (peerResponse.getFiles() == null) {
                        log.info("Пир {} не вернул файлов. Пропускаем", peerResponse.getUsername());
                        continue;
                    }

                    for (SoulseekFileNodeDto fileNode : peerResponse.getFiles()) {
                        String uniqueKey = peerResponse.getUsername() + ":" + fileNode.getFilename();
                        aggregatedFiles.put(uniqueKey, fileNode);
                    }
                }
            } catch (Exception e) {
                log.error("Ошибка пуллинга данных для одной из тасок", e);
            }
        }

        return new ArrayList<>(aggregatedFiles.values());
    }
}
