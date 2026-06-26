alter table slsk_download
    add column temp_cleaned     boolean not null default false,
    add column last_clean_error text    null;