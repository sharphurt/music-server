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
public class SlskSearchService {

    private final SlskRestService restService;
    private final ITunesSearchService iTunesSearchService;
    private final SoulseekTaskCacheService cacheService;
    private final SlskScoringService scoringService;

    public List<SlskSearchTaskDto> initiateSearch(long trackId) {
        TrackDto trackDto = iTunesSearchService.searchTrackById(trackId);
        List<SlskSearchTaskDto> searchTasks = trackDto.getTitleAliases().stream().flatMap(
                alias -> Stream.of(trackDto.getArtistName() + " " + alias,
                    alias + " " + trackDto.getArtistName(), alias))
            .map(e -> DataClearingUtils.normalizeString(e).toLowerCase()).filter(e -> !e.isEmpty())
            .distinct().map(query -> createSearchTask(trackDto, query)).filter(Objects::nonNull)
            .toList();

        for (SlskSearchTaskDto searchTask : searchTasks) {
            cacheService.save(searchTask);
        }

        return searchTasks;
    }

    public SlskSearchTaskDto createSearchTask(TrackDto trackDto, String query) {
        UUID searchId = UUID.randomUUID();
        boolean isSuccessful = restService.postSearchTask(searchId, query);
        if (!isSuccessful) {
            log.info("Задача на поиск для запроса {} не была создана, будет пропущена", query);
            return null;
        }

        return new SlskSearchTaskDto(trackDto.getITunesId(), query, searchId);
    }

    public List<SlskFileScoreDto> fetchSearchResults(long trackId, int maxResults) {
        TrackDto trackDto = iTunesSearchService.searchTrackById(trackId);
        List<SlskFileNodeDto> filesData = collectFiles(trackId);

        return scoringService.matchAndSort(trackDto, filesData).stream().limit(maxResults).toList();
    }

    private List<SlskFileNodeDto> collectFiles(long trackId) {
        List<SlskSearchTaskDto> tasksData = cacheService.getAllForTrack(trackId);

        if (tasksData == null || tasksData.isEmpty()) {
            log.info("Для трека id={} нет активных задач на поиск", trackId);
            return List.of();
        }

        Map<String, SlskFileNodeDto> aggregatedFiles = new HashMap<>();

        for (SlskSearchTaskDto task : tasksData) {
            try {
                Optional<SlskSearchResultDto> searchResult = restService.getSearchResult(
                    task.uuid());

                if (searchResult.isEmpty()) {
                    log.error("Для задачи {} Soulseek ничего не вернул. Пропускаем", task);
                    cacheService.delete(task);
                    continue;
                }

                for (SlskPeerResponseDto peerResponse : searchResult.get().getResponses()) {
                    if (peerResponse.getFiles() == null) {
                        log.info("Пир {} не вернул файлов. Пропускаем", peerResponse.getUsername());
                        continue;
                    }

                    for (SlskFileNodeDto fileNode : peerResponse.getFiles()) {
                        String uniqueKey =
                            peerResponse.getUsername() + ":" + fileNode.getFilename();
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
