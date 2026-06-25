package ru.sharphurt.musicserver.api.search.dto;


import ru.sharphurt.musicserver.common.entity.EntityType;

public record SearchRequestDto(
    String query,
    long limit,
    long page,
    EntityType type
) {

}
