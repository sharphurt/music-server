package ru.sharphurt.musicserver.search.enrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrichmentExecutionService<T> {

    private final Executor enrichmentExecutor;

    private final List<EnrichmentService<T>> enrichmentServices;

    public List<T> enrich(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        CompletableFuture<?>[] futures = entities.stream()
                .map(dto -> CompletableFuture.runAsync(() -> enrichSingleEntity(dto), enrichmentExecutor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        return entities;
    }

    private void enrichSingleEntity(T entity) {
        for (EnrichmentService<T> service : enrichmentServices) {
            try {
                service.enrich(entity);
            } catch (Exception e) {
                log.error("Ошибка при обогащении сущности сервисом: {}", service.getClass().getSimpleName(), e);
            }
        }
    }
}