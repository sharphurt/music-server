package ru.sharphurt.musicserver.dataenrichment;

public interface EnrichmentService<T> {

    T enrich(T entity);
}
