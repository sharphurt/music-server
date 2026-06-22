package ru.sharphurt.musicserver.soulseek.dto.rest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

//TODO: remove unnecessary fields
@Data
@Builder
public class SlskDownloadResponseDto {

    private UUID uuid;
    private String username;
    private String filename;
    private long size;
    private String state;
    private LocalDateTime requestedAt;
    private long bytesTransferred;
    private int percentComplete;
    private long trackId;
    private String trackName;
    private String artistName;
    private Duration elapsedTime;
    private Duration remainingTime;
    private double averageSpeed;
    private LocalDateTime enqueuedAt;
}
