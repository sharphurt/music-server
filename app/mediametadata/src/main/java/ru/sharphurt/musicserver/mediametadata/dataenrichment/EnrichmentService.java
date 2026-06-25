package ru.sharphurt.musicserver.mediametadata.dataenrichment;

public interface EnrichmentService<T> {

    T enrich(T entity);
}
