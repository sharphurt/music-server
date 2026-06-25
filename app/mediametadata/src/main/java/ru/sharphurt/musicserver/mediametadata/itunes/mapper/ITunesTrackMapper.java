package ru.sharphurt.musicserver.mediametadata.itunes.mapper;

import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.mediametadata.itunes.dto.ITunesTrackDto;

@Mapper(componentModel = "spring", builder = @Builder)
public interface ITunesTrackMapper {

    @Mapping(target = "iTunesId", source = "trackId")
    @Mapping(target = "albumId", source = "collectionId")
    @Mapping(target = "title", source = "trackName")
    @Mapping(target = "albumArtistName", source = "artistName")
    @Mapping(target = "albumName", source = "collectionName")
    @Mapping(target = "duration", source = "trackTimeMillis")
    @Mapping(target = "isExplicit", expression = "java(\"explicit\".equalsIgnoreCase(dto.getTrackExplicitness()))")
    @Mapping(target = "genres", expression = "java(mapGenres(dto.getPrimaryGenreName()))")
    @Mapping(target = "imageUrls", expression = "java(mapImageUrls(dto.getArtworkUrl100()))")
    @Mapping(target = "mbid", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "titleAliases", ignore = true)
    @Mapping(target = "artistNameAliases", ignore = true)
    @Mapping(target = "lyrics", ignore = true)
    @Mapping(target = "trackStatus", ignore = true)
    @Mapping(target = "fullPath", ignore = true)
    @Mapping(target = "artist", ignore = true)
    @Mapping(target = "album", ignore = true)
    TrackEntity toTrackEntity(ITunesTrackDto dto);

    default List<TrackEntity> toTrackEntityList(List<ITunesTrackDto> dtos) {
        return dtos.stream().map(this::toTrackEntity).toList();
    }

    default List<String> mapImageUrls(String artworkUrl100) {
        if (artworkUrl100 == null) {
            return List.of();
        }
        return List.of(artworkUrl100.replace("100x100bb.jpg", "600x600bb.jpg"));
    }

    default List<String> mapGenres(String primaryGenreName) {
        if (primaryGenreName == null) {
            return List.of();
        }
        return List.of(primaryGenreName);
    }
}