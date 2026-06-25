package ru.sharphurt.musicserver.mediametadata.itunes;

import lombok.Getter;

@Getter
public enum ITunesEntityType {
    SONG("song"),
    ALBUM("collection"),
    ARTIST("musicArtist");

    private final String apiName;

    ITunesEntityType(String song) {
        this.apiName = song;
    }
}
