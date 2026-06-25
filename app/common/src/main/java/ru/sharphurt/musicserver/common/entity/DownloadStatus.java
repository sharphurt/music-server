package ru.sharphurt.musicserver.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DownloadStatus {
    QUEUED(false),
    FAILED(false),
    COMPLETED(true),
    MOVING(true),
    IN_LIBRARY(true);

    private final boolean success;
}
