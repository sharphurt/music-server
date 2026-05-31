package ru.sharphurt.musicserver.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BaseEntityDto {
    private long itunesId;

    private String title;

    private List<String> genres;

    private ArrayList<String> imageUrls;

    private String downloadUrl;

    private String mbid;

}
