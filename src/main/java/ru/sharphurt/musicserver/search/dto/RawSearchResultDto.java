package ru.sharphurt.musicserver.search.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RawSearchResultDto<T> {

    private long totalCount;

    private long page;

    private long pageSize;

    private String query;

    private List<T> entities;

    public static <T> RawSearchResultDto<T> withContent(List<T> entities, long totalCount, String query, long pageSize, long page) {
        return RawSearchResultDto.<T>builder()
                .totalCount(totalCount)
                .pageSize(pageSize)
                .page(page)
                .query(query)
                .entities(entities)
                .build();

    }

    public static <T> RawSearchResultDto<T> empty(SearchRequestDto searchRequestDto) {
        return RawSearchResultDto.<T>builder()
                .totalCount(0)
                .pageSize(searchRequestDto.getLimit())
                .page(searchRequestDto.getPage())
                .query(searchRequestDto.getQuery())
                .entities(List.of())
                .build();

    }
}
