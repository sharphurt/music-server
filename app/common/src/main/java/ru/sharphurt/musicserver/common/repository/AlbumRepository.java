package ru.sharphurt.musicserver.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.sharphurt.musicserver.common.entity.AlbumEntity;

public interface AlbumRepository extends JpaRepository<AlbumEntity, Long> {

    @Query(value = """
        INSERT INTO album (
            album_id, album_name, album_type, image_url, is_explicit, track_count, country, release_date, artist_id, artist_name, primary_genre_name
        ) VALUES (
            :#{#album.albumId}, :#{#album.albumName}, :#{#album.albumType}, :#{#album.imageUrl}, :#{#album.explicit}, 
            :#{#album.trackCount}, :#{#album.country}, :#{#album.releaseDate}, :#{#album.artist.artistId}, :#{#album.artistName}, :#{#album.primaryGenreName}
        )
        ON CONFLICT DO NOTHING 
        """, nativeQuery = true)
    @Modifying
    void upsert(AlbumEntity album);
}
