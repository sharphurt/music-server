package ru.sharphurt.musicserver.mediametadata.itunes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.time.ZonedDateTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ITunesCollectionDto extends ITunesResultDto {

    private String collectionType;
    private Long collectionId;
    private String collectionName;
    private String collectionCensoredName;
    private String collectionViewUrl;
    private String artworkUrl60;
    private String artworkUrl100;
    private double collectionPrice;
    private String collectionExplicitness;
    private String contentAdvisoryRating;
    private Integer trackCount;
    private String copyright;
    private String country;
    private String currency;
    private ZonedDateTime releaseDate;
}