package ru.sharphurt.musicserver.mediametadata.itunes.mapper;

import org.mapstruct.Mapper;
import ru.sharphurt.musicserver.common.entity.ArtistEntity;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesArtistDto;

@Mapper(componentModel = "spring")
public interface ITunesArtistMapper {

    ArtistEntity toArtistEntity(ITunesArtistDto dto);
}
