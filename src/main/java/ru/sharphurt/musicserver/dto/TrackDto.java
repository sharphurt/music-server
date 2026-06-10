package ru.sharphurt.musicserver.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Data
public class TrackDto {

    private Long iTunesId;
    private Long artistId;
    private Long albumId;

    private String title;
    private String artistName;
    private String albumArtistName;
    private String albumName;

    private Integer trackNumber;
    private Integer discNumber;

    private List<String> genres;
    private List<String> imageUrls;
    private String downloadUrl;
    private String previewUrl;

    private String mbid;
    private long playcounts;
    private long duration;
    private ZonedDateTime releaseDate;
    private boolean isExplicit;

    private HashSet<String> titleAliases;
    private HashSet<String> artistNameAliases;

    public TrackDto addTitleAliases(Set<String> titleAliases) {
        if (this.titleAliases == null) {
            this.titleAliases = new HashSet<>(List.of(title));
        }
        this.titleAliases.addAll(titleAliases);
        return this;
    }

    public TrackDto addArtistAliases(Set<String> artistAliases) {
        if (this.artistNameAliases == null) {
            this.artistNameAliases = new HashSet<>(List.of(artistName));
        }
        this.artistNameAliases.addAll(artistAliases);
        return this;
    }

    @Override
    public String toString() {
        return "%s %s %s".formatted(title, artistName, albumName);
    }
}