package ru.sharphurt.musicserver.mediametadata.itunes.service;

import lombok.Getter;

@Getter
public enum ITunesEntityType {
    SONG("song");


    private final String apiName;

    ITunesEntityType(String song) {
        this.apiName = song;
    }
}
