package ru.sharphurt.musicserver.mediametadata.itunes.dto;

import java.util.List;

public record ITunesAlbumTracksResult(
    ITunesCollectionDto album,
    List<ITunesTrackDto> tracks
) {

}