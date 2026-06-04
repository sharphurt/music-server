package ru.sharphurt.musicserver.soulseek.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Data;
import ru.sharphurt.musicserver.util.DurationDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlskFileTransferDto {

    private String id;
    private String username;
    private String direction;
    private String filename;
    private long size;
    private long startOffset;
    private String state;
    private String stateDescription;
    private LocalDateTime requestedAt;
    private LocalDateTime enqueuedAt;
    private Instant startedAt;
    private Instant endedAt;
    private long bytesTransferred;
    private double averageSpeed;
    private int attempts;
    private long bytesRemaining;
    private int percentComplete;

    @JsonDeserialize(using = DurationDeserializer.class)
    private Duration elapsedTime;

    @JsonDeserialize(using = DurationDeserializer.class)
    private Duration remainingTime;
}
