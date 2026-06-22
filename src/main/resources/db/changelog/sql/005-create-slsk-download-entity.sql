create table slsk_download
(
    id          bigint primary key,
    transfer_id uuid not null,
    user_id     bigint references users (id)        default null,
    track_id    bigint references track (itunes_id) default null
);