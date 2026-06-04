package ru.sharphurt.musicserver.soulseek.dto;

import lombok.Data;

import java.util.List;

@Data
public class SlskPeerResponseDto {
    private String username;
    private int token;
    private int fileCount;
    private long uploadSpeed;
    private int queueLength;
    private boolean hasFreeUploadSlot;
    private int lockedFileCount;
    private List<SlskFileNodeDto> lockedFiles;
    private List<SlskFileNodeDto> files;

}