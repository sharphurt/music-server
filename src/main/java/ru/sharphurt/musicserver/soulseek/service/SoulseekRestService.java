package ru.sharphurt.musicserver.soulseek.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekFileNodeDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekPeerResponseDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekSearchResultDto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
            Map<String, Object> body = new HashMap<>();
            body.put("id", uuid);
            body.put("searchText", query);

            restClient.post()
                    .uri("/api/v0/searches")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (Exception e) {
            log.error("Не удалось создать задачу на поиск Soulseek. uuid: {}, query: {}", uuid, query, e);
            return false;
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
                }
            }

            return Optional.of(searchResultDto);
        } catch (Exception e) {
            log.error("Ошибка получения результатов поиска по uuid {}", uuid, e);
            return Optional.empty();
        }
    }
}