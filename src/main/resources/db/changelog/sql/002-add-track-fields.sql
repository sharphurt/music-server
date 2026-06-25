alter table track
    add column track_status varchar(30) not null default 'NOT_DOWNLOADED';

alter table track
    add column full_path text null default null;