create table slsk_download
(
    uuid        uuid primary key                    default gen_random_uuid(),
    transfer_id uuid                         not null,
    user_id     bigint references users (id) not null,
    track_id    bigint references track (itunes_id) default null
);