package ru.sharphurt.musicserver.locallibrary.enitiy;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lyrics")
public class LyricsEntity {

    @Id
    private Long id;

}
