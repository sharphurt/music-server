package ru.sharphurt.musicserver.mediametadata.exception;

import lombok.Getter;

@Getter
public class AlbumNotFoundException extends RuntimeException {

    private final long albumId;

    public AlbumNotFoundException(long albumId) {
        super("Album with id " + albumId + " not found");
        this.albumId = albumId;
    }

}
