package ru.sharphurt.musicserver.mediametadata.itunes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "wrapperType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ITunesCollectionDto.class, name = "collection"),
    @JsonSubTypes.Type(value = ITunesTrackDto.class, name = "track"),
    @JsonSubTypes.Type(value = ITunesArtistDto.class, name = "artist")
})
public abstract class ITunesResultDto {

    private String wrapperType;
    private Long artistId;
    private String artistName;
    private String artistViewUrl;
    private String primaryGenreName;
}