package ru.sharphurt.musicserver.soulseek.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sharphurt.musicserver.soulseek.dto.SlskDownloadResponseDto;
import ru.sharphurt.musicserver.common.entity.SlskDownloadEntity;
import ru.sharphurt.musicserver.soulseek.service.SlskRestService;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SlskDownloadDtoMapper {

    private final SlskRestService restService;

    public SlskDownloadResponseDto mapToResponseDto(SlskDownloadEntity downloadEntity) {
        SlskDownloadResponseDto.SlskDownloadResponseDtoBuilder responseBuilder = SlskDownloadResponseDto.builder()
            .uuid(downloadEntity.getUuid())
            .username(downloadEntity.getSlskUsername())
            .filename(downloadEntity.getTrackMetadata().getFullPath())
            .size(downloadEntity.getSlskFilesize())
            .state(downloadEntity.getDownloadStatus().name())
            .requestedAt(downloadEntity.getRequestedAt())
            .bytesTransferred(
                downloadEntity.getDownloadStatus().isSuccess()
                    ? downloadEntity.getSlskFilesize()
                    : 0L
            )
            .percentComplete(downloadEntity.getDownloadStatus().isSuccess()
                ? 100
                : 0)
            .trackId(downloadEntity.getTrackMetadata().getITunesId())
            .trackName(downloadEntity.getTrackMetadata().getTitle())
            .artistName(downloadEntity.getTrackMetadata().getArtistName())
            .elapsedTime(Duration.ZERO)
            .remainingTime(Duration.ZERO)
            .enqueuedAt(LocalDateTime.now())
            .averageSpeed(0);

        restService.getDownloadByTransferId(downloadEntity.getTransferId().toString()).ifPresent(
            transferDto -> responseBuilder
                .bytesTransferred(transferDto.getBytesTransferred())
                .percentComplete(transferDto.getPercentComplete())
                .elapsedTime(transferDto.getElapsedTime())
                .remainingTime(transferDto.getRemainingTime())
                .enqueuedAt(transferDto.getEnqueuedAt())
                .averageSpeed(transferDto.getAverageSpeed()));

        return responseBuilder.build();
    }

}
