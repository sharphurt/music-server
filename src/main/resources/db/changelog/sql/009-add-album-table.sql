create table album
(
    album_id           bigint primary key,
    artist_id          bigint  not null references artist (artist_id),
    artist_name        text,
    album_name         text,
    album_type         varchar(50),
    image_url          text,
    is_explicit        boolean not null default false,
    track_count        integer,
    country            varchar(50),
    primary_genre_name varchar(100),
    release_date       timestamptz
);