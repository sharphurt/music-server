alter table slsk_search_task
    add constraint fk_track
        foreign key (track_id) references track (itunes_id);