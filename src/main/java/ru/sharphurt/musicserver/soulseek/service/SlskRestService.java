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
import ru.sharphurt.musicserver.soulseek.dto.SlskTransferDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskSearchResultDto;
import ru.sharphurt.musicserver.soulseek.dto.rest.SlskPeerResponseDto;
import ru.sharphurt.musicserver.soulseek.dto.rest.SlskTransfersResponseDto;

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

    public Optional<SlskSearchResultDto>  getSearchResult(UUID uuid) {
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

    public Optional<SlskTransferDto> getDownloadByTransferId(String transferId) {
        try {
            SlskTransferDto slskDownloadsResultDto = restClient.get()
                .uri("/api/v0/transfers/downloads/batches/" + transferId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

            if (slskDownloadsResultDto == null) {
                return Optional.empty();
            }

            return Optional.of(slskDownloadsResultDto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}