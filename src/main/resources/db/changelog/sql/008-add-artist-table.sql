create table artist
(
    artist_id          bigint primary key,
    artist_name        text,
    primary_genre_name varchar(100),
    artist_type        varchar(50)
);
