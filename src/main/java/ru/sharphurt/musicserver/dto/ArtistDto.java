package ru.sharphurt.musicserver.dto;

import java.util.List;

public record ArtistDto(
        long iTunesId,
        String title,
        List<String> genres,
        List<String> imageUrls,
        String downloadUrl,
        String mbid,
        Integer listeners
) {}
