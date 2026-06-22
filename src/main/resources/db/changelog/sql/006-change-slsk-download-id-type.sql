alter table slsk_download
    drop column id;

alter table slsk_download
    add column "uuid" uuid;