package ru.sharphurt.musicserver.soulseek.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SoulseekSearchService {

    private final RestClient restClient;

    public SoulseekSearchService(@Value("${slskd.url}") String slskdBaseUrl,
                                 @Value("${slskd.api-key}") String slskdApiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(slskdBaseUrl)
                .defaultHeader("X-API-Key", slskdApiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String initiateSearch(String query) {
        String searchId = UUID.randomUUID().toString();

        Map<String, Object> body = new HashMap<>();
        body.put("id", searchId);
        body.put("searchText", query);

        restClient.post()
                .uri("/api/v0/searches")
                .body(body)
                .retrieve()
                .toBodilessEntity();

        return searchId;
    }

    public String fetchSearchResults(String searchId) {
        return restClient.get()
                .uri("/api/v0/searches/{id}?includeResponses=true", searchId)
                .retrieve()
                .body(String.class);
    }
}
