package ru.sharphurt.musicserver.search.enrichment;

public interface EnrichmentService<T> {

    void enrich(T entity);
}
