package ru.sharphurt.musicserver.search.dto;

import lombok.Data;

@Data
public class SearchRequestDto {
    private String query;
    private long limit;
    private long page;
}
