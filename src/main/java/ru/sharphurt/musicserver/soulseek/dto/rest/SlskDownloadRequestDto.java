package ru.sharphurt.musicserver.soulseek.dto.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sharphurt.musicserver.soulseek.entity.DownloadIntent;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlskDownloadRequestDto {

    @JsonProperty(value = "trackId")
    long trackId;

    @JsonProperty(value = "filename")
    String filename;

    @JsonProperty(value = "username")
    String username;

    @JsonProperty(value = "size")
    long size;

    @JsonProperty(value = "intent")
    DownloadIntent downloadIntent;
}