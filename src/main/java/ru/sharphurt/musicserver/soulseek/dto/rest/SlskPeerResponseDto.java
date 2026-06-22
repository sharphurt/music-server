package ru.sharphurt.musicserver.soulseek.dto.rest;

import lombok.Data;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileNodeDto;

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