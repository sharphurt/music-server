package ru.sharphurt.musicserver.soulseek.dto.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SlskDownloadRequestDto(
    @JsonProperty(value = "trackId") long trackId,
    @JsonProperty(value = "filename") String fileName,
    @JsonProperty(value = "username") String userName,
    @JsonProperty(value = "size") long size) {

}
