ALTER TABLE slsk_download
    ADD COLUMN intent VARCHAR(10) NOT NULL DEFAULT 'ADD';
ALTER TABLE slsk_download
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'QUEUED';
ALTER TABLE slsk_download
    ADD COLUMN local_filename TEXT;
ALTER TABLE slsk_download
    ADD COLUMN library_path TEXT;
ALTER TABLE slsk_download
    ADD COLUMN error_message TEXT;

ALTER TABLE slsk_download
    ADD COLUMN slsk_username VARCHAR(255);
ALTER TABLE slsk_download
    ADD COLUMN slsk_filename TEXT;
ALTER TABLE slsk_download
    ADD COLUMN slsk_filesize BIGINT;