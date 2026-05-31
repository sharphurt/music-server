package ru.sharphurt.musicserver.search.dto;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class AlbumDto extends BaseEntityDto {

    private String authorName;
}
