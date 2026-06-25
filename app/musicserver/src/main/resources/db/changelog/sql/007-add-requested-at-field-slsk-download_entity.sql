alter table slsk_download
    add column requested_at timestamp not null default now();