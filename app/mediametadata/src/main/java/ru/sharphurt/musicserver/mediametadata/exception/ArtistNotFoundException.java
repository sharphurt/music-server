package ru.sharphurt.musicserver.mediametadata.exception;

import lombok.Getter;

@Getter
public class ArtistNotFoundException extends RuntimeException {

    private final long artistId;

    public ArtistNotFoundException(long artistId) {
        super("Artist with id " + artistId + " not found");
        this.artistId = artistId;
    }

}
