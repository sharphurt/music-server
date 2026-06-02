package ru.sharphurt.musicserver.search.dto;

import ru.sharphurt.musicserver.dto.EntityType;

import java.util.List;

public record SearchResponseDto<T>(
        EntityType entityType,
        long totalResults,
        long pageSize,
        long page,
        List<T> entities,
        String query
) {
    public static <T> SearchResponseDto<T> withContent(EntityType entityType, List<T> entities, long totalCount, String query, long pageSize, long page) {
        return new SearchResponseDto<>(
                entityType,
                totalCount,
                pageSize,
                page,
                entities,
                query
        );
    }

    public static <T> SearchResponseDto<T> empty(SearchRequestDto searchRequestDto) {
        return new SearchResponseDto<>(
                searchRequestDto.type(),
                0,
                searchRequestDto.limit(),
                0,
                List.of(),
                searchRequestDto.query()
        );

    }
}
