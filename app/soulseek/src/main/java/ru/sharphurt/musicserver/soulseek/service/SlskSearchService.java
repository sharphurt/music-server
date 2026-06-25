package ru.sharphurt.musicserver.soulseek.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.common.entity.SlskSearchTaskEntity;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.common.repository.SlskSearchTaskRepository;
import ru.sharphurt.musicserver.mediametadata.db.MetadataService;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileNodeDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileScoreDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskPeerResponseDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskSearchResultDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlskSearchService {

    private final SlskRestService restService;
    private final SlskScoringService scoringService;
    private final SlskSearchTaskRepository slskSearchTaskRepository;
    private final MetadataService mediaMetadataService;


    public List<SlskSearchTaskEntity> initiateSearch(long trackId) {
        TrackEntity trackDto = mediaMetadataService.findOrFetchTrack(trackId);
        List<SlskSearchTaskEntity> alreadyCreatedTasks = slskSearchTaskRepository.findAllByTrackIdAndDisabledFalse(
            trackId);

        if (alreadyCreatedTasks.stream().anyMatch(e -> e.getQuery().equalsIgnoreCase(trackDto.getTitle()))) {
            log.info("Задача на поиск уже существует");
            return alreadyCreatedTasks;
        }

        SlskSearchTaskEntity searchTask = createSearchTask(trackDto, trackDto.getTitle());
//        List<SlskSearchTaskEntity> searchTasks = trackDto.getTitleAliases()
//            .stream()
//            .map(e -> DataClearingUtils.normalizeString(e).toLowerCase()).filter(e -> !e.isEmpty())
//            .distinct()
//            .filter(query -> alreadyCreatedTasks.stream()
//                .noneMatch(act -> act.getQuery().equalsIgnoreCase(query)))
//            .map(query -> createSearchTask(trackDto, query))
//            .filter(Objects::nonNull)
//            .toList();

        slskSearchTaskRepository.save(searchTask);

        return List.of(searchTask);
    }

    public SlskSearchTaskEntity createSearchTask(TrackEntity trackDto, String query) {
        UUID searchId = UUID.randomUUID();
        boolean isSuccessful = restService.postSearchTask(searchId, query);
        if (!isSuccessful) {
            log.info("Задача на поиск для запроса {} не была создана, будет пропущена", query);
            return null;
        }

        return SlskSearchTaskEntity.builder()
            .query(query)
            .trackId(trackDto.getITunesId())
            .uuid(searchId)
            .build();
    }

    public List<SlskFileScoreDto> fetchSearchResults(long trackId, int maxResults) {
        TrackEntity trackDto = mediaMetadataService.findOrFetchTrack(trackId);
        List<SlskFileNodeDto> filesData = collectFiles(trackId);

        return scoringService.matchAndSort(trackDto, filesData).stream().limit(maxResults).toList();
    }

    private List<SlskFileNodeDto> collectFiles(long trackId) {
        List<SlskSearchTaskEntity> tasksData = slskSearchTaskRepository.findAllByTrackIdAndDisabledFalse(
            trackId);

        if (tasksData == null || tasksData.isEmpty()) {
            log.info("Для трека id={} нет активных задач на поиск", trackId);
            return List.of();
        }

        Map<String, SlskFileNodeDto> aggregatedFiles = new HashMap<>();

        for (SlskSearchTaskEntity task : tasksData) {
            try {
                Optional<SlskSearchResultDto> searchResult = restService.getSearchResult(
                    task.getUuid());

                if (searchResult.isEmpty()) {
                    log.error("Для задачи {} Soulseek ничего не вернул. Пропускаем", task);
                    task.setDisabled(true);
                    slskSearchTaskRepository.save(task);
                    continue;
                }

                for (SlskPeerResponseDto peerResponse : searchResult.get().getResponses()) {
                    if (peerResponse.getFiles() == null) {
                        log.info("Пир {} не вернул файлов. Пропускаем", peerResponse.getUsername());
                        continue;
                    }

                    for (SlskFileNodeDto fileNode : peerResponse.getFiles()) {
                        if (!isFileCorrect(fileNode)) {
                            continue;
                        }

                        String uniqueKey =
                            peerResponse.getUsername() + ":" + fileNode.getFilename();
                        fileNode.setTrackId(trackId);
                        aggregatedFiles.put(uniqueKey, fileNode);
                    }
                }
            } catch (Exception e) {
                log.error("Ошибка пуллинга данных для одной из тасок", e);
            }
        }

        return new ArrayList<>(aggregatedFiles.values());
    }

    private boolean isFileCorrect(SlskFileNodeDto fileNode) {
        return fileNode.getFilename().endsWith(".mp3")
            || fileNode.getFilename().endsWith(".flac")
            || fileNode.getFilename().endsWith(".ogg");
    }
}
