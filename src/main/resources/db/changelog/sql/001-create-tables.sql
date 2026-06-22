CREATE TABLE IF NOT EXISTS lyrics
(
    id BIGINT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS genre
(
    id   BIGINT PRIMARY KEY,
    name TEXT
);

CREATE TABLE IF NOT EXISTS tag
(
    id   BIGINT PRIMARY KEY,
    name TEXT
);

CREATE TABLE IF NOT EXISTS slsk_search_task
(
    uuid     UUID PRIMARY KEY,
    track_id BIGINT  NOT NULL,
    query    TEXT    NOT NULL,
    disabled BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS track
(
    i_tunes_id          BIGINT PRIMARY KEY,
    artist_id           BIGINT,
    album_id            BIGINT,
    title               TEXT,
    artist_name         TEXT,
    album_artist_name   TEXT,
    album_name          TEXT,
    track_number        INTEGER,
    disc_number         INTEGER,
    genres              TEXT[],
    tags                TEXT[],
    image_urls          TEXT[],
    preview_url         TEXT,
    mbid                TEXT,
    playcounts          BIGINT  NOT NULL DEFAULT 0,
    duration            BIGINT  NOT NULL DEFAULT 0,
    release_date        TIMESTAMPTZ,
    is_explicit         BOOLEAN NOT NULL DEFAULT FALSE,
    title_aliases       TEXT[],
    artist_name_aliases TEXT[],
    lyrics_id           BIGINT  REFERENCES lyrics (id) ON DELETE SET NULL
);