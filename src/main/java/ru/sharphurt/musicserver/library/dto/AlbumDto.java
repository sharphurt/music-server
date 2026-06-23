package ru.sharphurt.musicserver.library.dto;

import java.util.List;

public record AlbumDto(
    long iTunesId,
    String title,
    List<String> genres,
    List<String> imageUrls,
    String downloadUrl,
    String mbid,
    String authorName
) {

}
