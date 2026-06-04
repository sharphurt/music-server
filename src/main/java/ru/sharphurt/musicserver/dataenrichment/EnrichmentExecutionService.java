package ru.sharphurt.musicserver.dataenrichment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sharphurt.musicserver.async.AsyncExecutor;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrichmentExecutionService<T> {

    private final AsyncExecutor executor;

    private final List<EnrichmentService<T>> enrichmentServices;

    public T enrich(T entity) {
        return enrichSingleEntity(entity);
    }

    public List<T> enrich(List<T> entities) {
        return executor.callForMultipleArgumentsAsync(entities, this::enrichSingleEntity);
    }

    private T enrichSingleEntity(T entity) {
        for (EnrichmentService<T> service : enrichmentServices) {
            try {
                entity = service.enrich(entity);
            } catch (Exception e) {
                log.error("Ошибка при обогащении сущности сервисом: {}",
                    service.getClass().getSimpleName(), e);
            }
        }

        return entity;
    }
}