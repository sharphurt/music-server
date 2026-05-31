package ru.sharphurt.musicserver.search.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponseDto<T> {

    private EntityType entityType;

    private long totalResults;

    private long pageSize;

    private long page;

    private List<T> entities;

    private String query;

}
