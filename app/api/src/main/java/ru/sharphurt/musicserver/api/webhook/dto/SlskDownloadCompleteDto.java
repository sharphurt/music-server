package ru.sharphurt.musicserver.api.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sharphurt.musicserver.soulseek.dto.SlskTransferDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlskDownloadCompleteDto {

    @JsonProperty("type")
    private String type;

    @JsonProperty("version")
    private int version;

    @JsonProperty("localFilename")
    private String localFilename;

    @JsonProperty("remoteFilename")
    private String remoteFilename;

    @JsonProperty("transfer")
    private SlskTransferDto transfer;

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("timestamp")
    private OffsetDateTime timestamp;
}
