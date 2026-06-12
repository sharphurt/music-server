package ru.sharphurt.musicserver.soulseek.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileNodeDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileTransferDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskPeerResponseDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskSearchResultDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskTransfersResponseDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskUserDownloadDto;

@Slf4j
@Service
public class SlskRestService {

    private final RestClient restClient;

    public SlskRestService(@Value("${slskd.url}") String slskdBaseUrl,
        @Value("${slskd.api-key}") String slskdApiKey) {
        this.restClient = RestClient.builder()
            .baseUrl(slskdBaseUrl)
            .defaultHeader("X-API-Key", slskdApiKey)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    public boolean postSearchTask(UUID uuid, String query) {
        try {
            log.info("Creating search task. uuid: {}, query: {}", uuid, query);

            Map<String, Object> body = new HashMap<>();
            body.put("id", uuid);
            body.put("searchText", query);

            ResponseEntity<Void> response = restClient.post()
                .uri("/api/v0/searches")
                .body(body)
                .retrieve()
                .toBodilessEntity();

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Не удалось создать задачу на поиск Soulseek. uuid: {}, query: {}", uuid,
                query, e);
            return false;
        }
    }

    public Optional<SlskTransfersResponseDto> postDownloadTask(String fileName, String userName,
        long size) {
        try {
            log.info("Creating download task. FileName: {}, UserName: {}, Size: {}", fileName,
                userName, size);

            Map<String, Object> body = new HashMap<>();
            body.put("filename", fileName);
            body.put("size", size);

            ResponseEntity<SlskTransfersResponseDto> response = restClient.post()
                .uri("/api/v0/transfers/downloads/" + userName)
                .body(List.of(body))
                .retrieve()
                .toEntity(SlskTransfersResponseDto.class);

            if (response.getStatusCode().isError() || response.getBody() == null) {
                return Optional.empty();
            }

            return Optional.of(response.getBody());
        } catch (Exception e) {
            log.error("Не удалось создать задачу на загрузку трека. FileName: {}, UserName: {}",
                fileName, userName, e);
            return Optional.empty();
        }
    }

    public Optional<SlskSearchResultDto> getSearchResult(UUID uuid) {
        try {
            SlskSearchResultDto slskSearchResultDto = restClient.get()
                .uri("/api/v0/searches/{id}?includeResponses=true", uuid)
                .retrieve()
                .body(SlskSearchResultDto.class);

            if (slskSearchResultDto == null) {
                log.error("Не найдено таски на поиск по uuid {}", uuid);
                return Optional.empty();
            }

            for (SlskPeerResponseDto responseDto : slskSearchResultDto.getResponses()) {
                for (SlskFileNodeDto fileNodeDto : responseDto.getFiles()) {
                    fileNodeDto.setUsername(responseDto.getUsername());
                    fileNodeDto.setUploadSpeed(responseDto.getUploadSpeed());
                    int kbps = fileNodeDto.getLength() > 0
                        ? (int) ((fileNodeDto.getSize() * 8L) / (fileNodeDto.getLength() * 1000L))
                        : 0;
                    fileNodeDto.setKbps(kbps);
                }
            }

            return Optional.of(slskSearchResultDto);
        } catch (Exception e) {
            log.error("Ошибка получения результатов поиска по uuid {}", uuid, e);
            return Optional.empty();
        }
    }

    public List<SlskUserDownloadDto> getDownloads() {
        try {
            List<SlskUserDownloadDto> slskDownloadsResultDto = restClient.get()
                .uri("/api/v0/transfers/downloads")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

            if (slskDownloadsResultDto == null) {
                log.error("Не удалось получить список загрузок");
                return List.of();
            }

            return slskDownloadsResultDto;
        } catch (Exception e) {
            log.error("Ошибка получения списка загрузок", e);
            return List.of();
        }
    }


    public Optional<SlskFileTransferDto> getDownloadById(String id) {
        try {
            SlskFileTransferDto slskDownloadsResultDto = restClient.get()
                .uri("/api/v0/transfers/downloads/batches/" + id)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

            if (slskDownloadsResultDto == null) {
                log.error("Не удалось получить данные о загрузке {}", id);
                return Optional.empty();
            }

            return Optional.of(slskDownloadsResultDto);
        } catch (Exception e) {
            log.error("Ошибка получения данных о загрузке {}", id, e);
            return Optional.empty();
        }
    }
}