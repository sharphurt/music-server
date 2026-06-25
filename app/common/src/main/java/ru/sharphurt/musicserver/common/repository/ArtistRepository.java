package ru.sharphurt.musicserver.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.sharphurt.musicserver.common.entity.ArtistEntity;

public interface ArtistRepository extends JpaRepository<ArtistEntity, Long> {

    @Query(value = """
        INSERT INTO artist (artist_id, artist_name, primary_genre_name, artist_type)
        VALUES (:#{#artist.artistId}, :#{#artist.artistName}, :#{#artist.primaryGenreName}, :#{#artist.artistType})
        ON CONFLICT DO NOTHING
        """, nativeQuery = true)
    @Modifying
    void upsert(ArtistEntity artist);
}
