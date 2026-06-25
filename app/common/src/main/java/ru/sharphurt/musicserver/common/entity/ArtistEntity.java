package ru.sharphurt.musicserver.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "artist")
public class ArtistEntity {

    @Id
    @Column(name = "artist_id")
    private Long artistId;

    @Column(name = "artist_name")
    private String artistName;

    @Column(name = "primary_genre_name")
    private String primaryGenreName;

    @Column(name = "artist_type")
    private String artistType;
}
