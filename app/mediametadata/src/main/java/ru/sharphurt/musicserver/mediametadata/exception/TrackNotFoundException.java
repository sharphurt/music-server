package ru.sharphurt.musicserver.mediametadata.exception;

import lombok.Getter;

@Getter
public class TrackNotFoundException extends RuntimeException {

    private final long trackId;

    public TrackNotFoundException(long trackId) {
        super("Track with id " + trackId + " not found");
        this.trackId = trackId;
    }

}
