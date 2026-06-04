package ru.sharphurt.musicserver.soulseek.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SlskDownloadRequestDto(@JsonProperty(value = "filename") String fileName,
                                     @JsonProperty(value = "username") String userName,
                                     @JsonProperty(value = "size") long size) {

}
