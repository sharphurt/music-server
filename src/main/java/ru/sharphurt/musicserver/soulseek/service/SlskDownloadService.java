package ru.sharphurt.musicserver.soulseek.service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.library.enitiy.TrackEntity;
import ru.sharphurt.musicserver.library.service.TrackDataService;
import ru.sharphurt.musicserver.soulseek.dto.SlskTransferDto;
import ru.sharphurt.musicserver.soulseek.dto.rest.SlskDownloadRequestDto;
import ru.sharphurt.musicserver.soulseek.dto.rest.SlskDownloadResponseDto;
import ru.sharphurt.musicserver.soulseek.dto.rest.SlskTransfersResponseDto;
import ru.sharphurt.musicserver.soulseek.entity.DownloadIntent;
import ru.sharphurt.musicserver.soulseek.entity.DownloadStatus;
import ru.sharphurt.musicserver.soulseek.entity.SlskDownloadEntity;
import ru.sharphurt.musicserver.soulseek.repository.SlskDownloadRepository;
import ru.sharphurt.musicserver.user.entity.UserEntity;
import ru.sharphurt.musicserver.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlskDownloadService {

    private final SlskRestService restService;
    private final SlskDownloadRepository slskDownloadRepository;
    private final TrackDataService trackDataService;
    private final UserRepository userRepository;

    @Transactional
    public SlskDownloadEntity enqueueDownload(SlskDownloadRequestDto dto) {
        Optional<SlskDownloadEntity> existing = slskDownloadRepository
            .findByUserAndSlskUsernameAndSlskFilenameAndDownloadStatusNotIn(
                userRepository.getReferenceById(1L),
                dto.getUsername(),
                dto.getFilename(),
                List.of(DownloadStatus.FAILED, DownloadStatus.IN_LIBRARY)
            );

        if (existing.isPresent()) {
            SlskDownloadEntity download = existing.get();
            if (dto.getDownloadIntent() == DownloadIntent.ADD
                && download.getDownloadIntent() == DownloadIntent.PLAY) {
                download.setDownloadIntent(DownloadIntent.ADD);
                log.info("Обновление Intent для {}. Будет добавлен в библиотеку", existing.get());
                slskDownloadRepository.save(download);
            }
            return download;
        }

        Optional<SlskTransfersResponseDto> responseDto = restService.postDownloadTask(
            dto.getFilename(),
            dto.getUsername(),
            dto.getSize());

        if (responseDto.isEmpty()) {
            log.error("Не удалось создать задачу на загрузку. DownloadRequestDto: {}",
                dto);
            return null;
        }

        Optional<SlskTransferDto> transferDto = responseDto.get().getEnqueued().stream()
            .filter(e -> Objects.equals(
                e.getFilename(), dto.getFilename()) && Objects.equals(
                e.getUsername(), dto.getUsername()))
            .findFirst();

        if (transferDto.isEmpty()) {
            log.error("Не удалось создать задачу на загрузку. DownloadRequestDto: {}",
                dto);
            return null;
        }

        TrackEntity track = trackDataService.getTrackData(dto.getTrackId());
        if (track == null) {
            log.error("Трек не найден ни в БД ни в ITunes. DownloadRequestDto: {}",
                dto);
            return null;
        }

        SlskDownloadEntity entity = SlskDownloadEntity.builder()
            .user(userRepository.getReferenceById(1L)) // TODO: add auth
            .transferId(UUID.fromString(transferDto.get().getId()))
            .trackMetadata(track)
            .downloadIntent(
                dto.getDownloadIntent() != null ? dto.getDownloadIntent() : DownloadIntent.PLAY)
            .downloadStatus(DownloadStatus.QUEUED)
            .slskUsername(dto.getUsername())
            .slskFilename(dto.getFilename())
            .slskFilesize(dto.getSize())
            .build();

        slskDownloadRepository.save(entity);

        return entity;
    }

    @Transactional
    public boolean requeueWithFallback(UUID downloadId) {
        SlskDownloadEntity download = slskDownloadRepository.findById(downloadId).orElseThrow();
        TrackEntity track = download.getTrackMetadata();

        SlskDownloadRequestDto originalDto = SlskDownloadRequestDto.builder()
            .filename(download.getSlskFilename())
            .username(download.getSlskUsername())
            .size(download.getSlskFilesize())
            .trackId(track.getITunesId())
            .downloadIntent(DownloadIntent.ADD)
            .build();

        SlskDownloadEntity result = enqueueDownload(originalDto);
        if (result == null) {
            log.error("Не удалось повторно загрузить файл по запросу {}", originalDto);
            return false;
        }

        return true;
    }

    public List<SlskDownloadResponseDto> getDownloads(UserEntity user) {
        List<SlskDownloadEntity> userDownloads = slskDownloadRepository.findAllByUser(user);

        return userDownloads.stream()
            .map(this::mapToResponseDto)
            .toList();
    }

    public SlskTransferDto getDownloadInfo(UUID downloadUuid, UserEntity user) {
        SlskDownloadEntity downloadEntity = slskDownloadRepository.findByUserAndUuid(user,
            downloadUuid);

        if (downloadEntity == null) {
            log.error("Failed to get download task info. TransferId: {}, User: {}", downloadUuid,
                user);
            return null;
        }

        Optional<SlskTransferDto> transferDto = restService.getDownloadByTransferId(
            downloadEntity.getTransferId().toString());

        if (transferDto.isEmpty()) {
            log.error("Failed to get download task info. TransferId: {}, User: {}", downloadUuid,
                user);
            return null;
        }

        return transferDto.get();
    }

    private SlskDownloadResponseDto mapToResponseDto(SlskDownloadEntity downloadEntity) {
        Optional<SlskTransferDto> responseDto = restService.getDownloadByTransferId(
            downloadEntity.getTransferId().toString());

        if (responseDto.isEmpty()) {
            return SlskDownloadResponseDto.builder()
                .uuid(downloadEntity.getUuid())
                .state("Not found")
                .trackId(downloadEntity.getTrackMetadata().getITunesId())
                .trackName(downloadEntity.getTrackMetadata().getTitle())
                .artistName(downloadEntity.getTrackMetadata().getArtistName())
                .filename(downloadEntity.getTrackMetadata().getFullPath())
                .enqueuedAt(LocalDateTime.now())
                .averageSpeed(0)
                .build();
        }

        return SlskDownloadResponseDto.builder()
            .uuid(downloadEntity.getUuid())
            .username(responseDto.get().getUsername())
            .filename(responseDto.get().getFilename())
            .size(responseDto.get().getSize())
            .state(responseDto.get().getState())
            .requestedAt(responseDto.get().getRequestedAt())
            .bytesTransferred(responseDto.get().getBytesTransferred())
            .percentComplete(responseDto.get().getPercentComplete())
            .trackId(downloadEntity.getTrackMetadata().getITunesId())
            .trackName(downloadEntity.getTrackMetadata().getTitle())
            .artistName(downloadEntity.getTrackMetadata().getArtistName())
            .elapsedTime(responseDto.get().getElapsedTime())
            .remainingTime(responseDto.get().getRemainingTime())
            .enqueuedAt(responseDto.get().getEnqueuedAt())
            .averageSpeed(responseDto.get().getAverageSpeed())
            .build();
    }
}
