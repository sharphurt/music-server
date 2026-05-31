package ru.sharphurt.musicserver.search.dto;

import java.util.ArrayList;
import java.util.List;

public class AlbumDto extends BaseEntityDto {

    private String authorName;

    public AlbumDto(String title, List<String> genres, String imageUrl, String downloadUrl, String mbid) {
        super(title, genres, new ArrayList<>(List.of(imageUrl)), downloadUrl, mbid);
    }
}
