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
    private int bitDepth;
    private int sampleRate;
    private int bitRate;
    private String username;
}
