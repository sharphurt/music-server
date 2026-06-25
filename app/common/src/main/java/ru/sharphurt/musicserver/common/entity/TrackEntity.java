package ru.sharphurt.musicserver.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "track")
public class TrackEntity {

    @Id
    @Column(name = "itunes_id")
    private Long iTunesId;

    @Column(name = "artist_id")
    private Long artistId;

    @Column(name = "album_id")
    private Long albumId;

    @Column(name = "title")
    private String title;

    @Column(name = "artist_name")
    private String artistName;

    @Column(name = "album_artist_name")
    private String albumArtistName;

    @Column(name = "album_name")
    private String albumName;

    @Column(name = "track_number")
    private Integer trackNumber;

    @Column(name = "disc_number")
    private Integer discNumber;

    // TODO: decompose
    @Column(name = "genres")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> genres;

    // TODO: decompose
    @Column(name = "tags")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> tags;

    @Column(name = "image_urls")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> imageUrls;

    @Column(name = "preview_url")
    private String previewUrl;

    @Column(name = "mbid")
    private String mbid;

    @Column(name = "duration")
    private long duration;

    @Column(name = "release_date")
    private ZonedDateTime releaseDate;

    @Column(name = "is_explicit")
    private boolean isExplicit;

    // TODO: decompose
    @Column(name = "title_aliases")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> titleAliases;

    // TODO: decompose
    @Column(name = "artist_name_aliases")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> artistNameAliases;

    @ManyToOne
    @JoinColumn(name = "lyrics_id")
    private LyricsEntity lyrics;

    @Column(name = "track_status")
    @Enumerated(EnumType.STRING)
    private TrackFileStatus trackStatus;

    @Column(name = "full_path")
    private String fullPath;

    public TrackEntity addTitleAliases(Set<String> titleAliases) {
        if (this.titleAliases == null) {
            this.titleAliases = new ArrayList<>(List.of(title));
        }
        this.titleAliases.addAll(titleAliases);
        return this;
    }

    public TrackEntity addArtistAliases(Set<String> artistAliases) {
        if (this.artistNameAliases == null) {
            this.artistNameAliases = new ArrayList<>(List.of(artistName));
        }
        this.artistNameAliases.addAll(artistAliases);
        return this;
    }

    @Override
    public String toString() {
        return "%s %s %s".formatted(title, artistName, albumName);
    }
}
