package ru.sharphurt.musicserver.soulseek.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekFileNodeDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekPeerResponseDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekSearchResultDto;
import ru.sharphurt.musicserver.soulseek.dto.TransfersResponseDto;

import java.util.*;

@Slf4j
@Service
public class SoulseekRestService {
    private final RestClient restClient;

    public SoulseekRestService(@Value("${slskd.url}") String slskdBaseUrl,
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
            log.error("Не удалось создать задачу на поиск Soulseek. uuid: {}, query: {}", uuid, query, e);
            return false;
        }
    }

    public Optional<TransfersResponseDto> postDownloadTask(SoulseekFileNodeDto fileNodeDto) {
        try {
            log.info("Creating download task. fileNodeDto: {}", fileNodeDto);

            Map<String, Object> body = new HashMap<>();
            body.put("filename", fileNodeDto.getFilename());
            body.put("size", fileNodeDto.getSize());

            ResponseEntity<TransfersResponseDto> response = restClient.post()
                    .uri("/api/v0/transfers/downloads/" + fileNodeDto.getUsername())
                    .body(List.of(body))
                    .retrieve()
                    .toEntity(TransfersResponseDto.class);

            if (response.getStatusCode().isError() || response.getBody() == null) {
                return Optional.empty();
            }

            return Optional.of(response.getBody());
        } catch (Exception e) {
            log.error("Не удалось создать задачу на загрузку трека. FileNodeDto: {}", fileNodeDto, e);
            return Optional.empty();
        }
    }

    public Optional<SoulseekSearchResultDto> getSearchResult(UUID uuid) {
        try {
            SoulseekSearchResultDto searchResultDto = restClient.get()
                    .uri("/api/v0/searches/{id}?includeResponses=true", uuid)
                    .retrieve()
                    .body(SoulseekSearchResultDto.class);

            if (searchResultDto == null) {
                log.error("Не найдено таски на поиск по uuid {}", uuid);
                return Optional.empty();
            }

            for (SoulseekPeerResponseDto responseDto : searchResultDto.getResponses()) {
                for (SoulseekFileNodeDto fileNodeDto : responseDto.getFiles()) {
                    fileNodeDto.setUsername(responseDto.getUsername());
                    fileNodeDto.setUploadSpeed(responseDto.getUploadSpeed());
                    int kbps = fileNodeDto.getLength() > 0
                            ? (int) ((fileNodeDto.getSize() * 8L) / (fileNodeDto.getLength() * 1000L))
                            : 0;
                    fileNodeDto.setKbps(kbps);
                }
            }

            return Optional.of(searchResultDto);
        } catch (Exception e) {
            log.error("Ошибка получения результатов поиска по uuid {}", uuid, e);
            return Optional.empty();
        }
    }

}