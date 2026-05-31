package ru.sharphurt.musicserver.search.dto;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ArtistDto extends BaseEntityDto {

    private Integer listeners;
}
