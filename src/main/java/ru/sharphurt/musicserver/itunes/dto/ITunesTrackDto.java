package ru.sharphurt.musicserver.itunes.dto;


import java.time.ZonedDateTime;

public record ITunesTrackDto(
        String wrapperType,
        String kind,
        Long artistId,
        Long collectionId,
        Long trackId,
        String artistName,
        String collectionName,
        String trackName,
        String collectionCensoredName,
        String trackCensoredName,
        String artistViewUrl,
        String collectionViewUrl,
        String trackViewUrl,
        String previewUrl,
        String artworkUrl30,
        String artworkUrl60,
        String artworkUrl100,
        Double collectionPrice,
        Double trackPrice,
        ZonedDateTime releaseDate,
        String collectionExplicitness,
        String trackExplicitness,
        Integer discCount,
        Integer discNumber,
        Integer trackCount,
        Integer trackNumber,
        Long trackTimeMillis,
        String country,
        String currency,
        String primaryGenreName,
        Boolean isStreamable
) {
}
