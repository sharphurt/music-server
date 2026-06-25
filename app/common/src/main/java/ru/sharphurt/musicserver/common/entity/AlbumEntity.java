package ru.sharphurt.musicserver.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "album")
public class AlbumEntity {

    @Id
    @Column(name = "album_id")
    private long albumId;

    @Column(name = "album_name")
    private String albumName;

    @Column(name = "album_type")
    private String albumType;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_explicit")
    private boolean explicit;

    @Column(name = "track_count")
    private int trackCount;

    @Column(name = "country")
    private String country;

    @Column(name = "release_date")
    private ZonedDateTime releaseDate;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private ArtistEntity artist;

    @Column(name = "artist_id", updatable = false, insertable = false)
    private long artistId;

    @Column(name = "artist_name")
    private String artistName;

    @Column(name = "primary_genre_name")
    private String primaryGenreName;
}