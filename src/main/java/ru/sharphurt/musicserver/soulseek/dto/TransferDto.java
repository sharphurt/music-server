package ru.sharphurt.musicserver.soulseek.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferDto {

    private UUID id;
    private String username;
    private String direction;
    private String filename;
    private long size;
    private long startOffset;
    private String state;
    private String stateDescription;
    private Instant requestedAt;
    private long bytesTransferred;
    private double averageSpeed;
    private int attempts;
    private long bytesRemaining;
    private double percentComplete;
}
