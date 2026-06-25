alter table track
    add constraint fk_track_artist
        foreign key (artist_id) references artist (artist_id),
    add constraint fk_track_album
        foreign key (album_id) references album (album_id);