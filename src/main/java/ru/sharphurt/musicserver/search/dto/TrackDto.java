package ru.sharphurt.musicserver.search.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
public class TrackDto extends BaseEntityDto {

    private String artistName;

    private String albumName;

    private long playcounts;

    private long duration;

    private LocalDateTime releaseDate;

    private String previewUrl;

    @Override
    public String toString() {
        return "Track: [%s] by [%s]".formatted(getTitle(), getArtistName());
    }
}
