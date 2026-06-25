package ru.sharphurt.musicserver.common.entity;

import java.util.List;

public record AlbumEntity(
    long iTunesId,
    String title,
    List<String> genres,
    List<String> imageUrls,
    String downloadUrl,
    String mbid,
    String authorName
) {

}
