package ru.sharphurt.musicserver.mediametadata.itunes.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sharphurt.musicserver.common.entity.AlbumEntity;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesCollectionDto;

@Mapper(componentModel = "spring")
public interface ITunesAlbumMapper {

    @Mapping(target = "albumId", source = "collectionId")
    @Mapping(target = "albumName", source = "collectionName")
    @Mapping(target = "imageUrl", expression = "java(mapImageUrl(dto.getArtworkUrl100()))")
    @Mapping(target = "explicit", expression = "java(\"explicit\".equalsIgnoreCase(dto.getCollectionExplicitness()))")
    @Mapping(target = "artist", ignore = true)
    AlbumEntity toAlbumEntity(ITunesCollectionDto dto);

    default String mapImageUrl(String artworkUrl100) {
        if (artworkUrl100 == null) {
            return null;
        }
        return artworkUrl100.replace("100x100bb.jpg", "600x600bb.jpg");
    }

}
