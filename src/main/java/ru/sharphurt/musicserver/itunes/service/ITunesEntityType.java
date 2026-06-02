package ru.sharphurt.musicserver.itunes.service;

import lombok.Getter;

@Getter
public enum ITunesEntityType {
    SONG("song");


    private final String apiName;

    ITunesEntityType(String song) {
        this.apiName = song;
    }
}
