package ru.sharphurt.musicserver.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DownloadIntent {
    PLAY(1),
    ADD(2);

    private final int priority;
}
