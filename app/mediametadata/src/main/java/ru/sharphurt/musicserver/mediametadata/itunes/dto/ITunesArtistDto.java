package ru.sharphurt.musicserver.mediametadata.itunes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ITunesArtistDto extends ITunesResultDto {

    private String artistType;
    private String artistLinkUrl;
    private Integer primaryGenreId;
}
