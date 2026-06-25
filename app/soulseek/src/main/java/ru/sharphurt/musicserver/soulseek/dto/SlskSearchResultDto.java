package ru.sharphurt.musicserver.soulseek.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class SlskSearchResultDto {

    private UUID id;
    private String searchText;
    private String state;
    private int token;
    private boolean isComplete;
    private int fileCount;
    private int responseCount;
    private int lockedFileCount;
    private Instant startedAt;
    private Instant endedAt;
    private List<SlskPeerResponseDto> responses;
}