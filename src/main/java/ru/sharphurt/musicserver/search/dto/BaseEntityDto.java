package ru.sharphurt.musicserver.search.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class BaseEntityDto {
    private long iTunesId;

    private String title;

    private List<String> genres;

    private List<String> imageUrls;

    private String downloadUrl;

    private String mbid;

}
