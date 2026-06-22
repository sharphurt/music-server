package ru.sharphurt.musicserver.search.dto;

import ru.sharphurt.musicserver.locallibrary.dto.EntityType;

public record SearchRequestDto(
    String query,
    long limit,
    long page,
    EntityType type
) {

}
