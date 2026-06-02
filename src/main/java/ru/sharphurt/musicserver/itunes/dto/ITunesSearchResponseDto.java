package ru.sharphurt.musicserver.itunes.dto;

import java.util.List;

public record ITunesSearchResponseDto(
        Long resultCount,
        List<ITunesTrackDto> results
) {
}
