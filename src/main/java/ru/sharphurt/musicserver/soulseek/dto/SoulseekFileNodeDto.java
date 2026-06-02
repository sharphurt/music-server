package ru.sharphurt.musicserver.soulseek.dto;

import lombok.Data;

@Data
public class SoulseekFileNodeDto {
    private String filename;
    private String extension;
    private long size;
    private int length;
    private int code;
    private boolean isLocked;
    private Integer bitDepth;
    private Integer sampleRate;
    private Integer bitRate;
    private String username;
}
