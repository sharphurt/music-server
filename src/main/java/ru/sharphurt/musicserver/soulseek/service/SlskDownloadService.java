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
import ru.sharphurt.musicserver.itunes.service.ITunesSearchService;
import ru.sharphurt.musicserver.locallibrary.enitiy.TrackEntity;
import ru.sharphurt.musicserver.locallibrary.enitiy.TrackFileStatus;
import ru.sharphurt.musicserver.locallibrary.repository.TrackRepository;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileTransferDto;
import ru.sharphurt.musicserver.soulseek.dto.rest.SlskDownloadRequestDto;
import ru.sharphurt.musicserver.soulseek.dto.rest.SlskDownloadResponseDto;
import ru.sharphurt.musicserver.soulseek.dto.rest.SlskTransfersResponseDto;
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
    private final TrackRepository trackRepository;
    private final ITunesSearchService itunesSearchService;
    private final UserRepository userRepository;

    @Transactional
    public SlskDownloadEntity enqueueDownload(SlskDownloadRequestDto slskDownloadRequestDto) {
        Optional<SlskTransfersResponseDto> responseDto = restService.postDownloadTask(
            slskDownloadRequestDto.fileName(),
            slskDownloadRequestDto.userName(),
            slskDownloadRequestDto.size());

        if (responseDto.isEmpty()) {
            log.error("Не удалось создать задачу на загрузку. DownloadRequestDto: {}",
                slskDownloadRequestDto);
            return null;
        }

        Optional<SlskFileTransferDto> transferDto = responseDto.get().getEnqueued().stream()
            .filter(e -> Objects.equals(
                e.getFilename(), slskDownloadRequestDto.fileName()) && Objects.equals(
                e.getUsername(), slskDownloadRequestDto.userName()))
            .findFirst();

        if (transferDto.isEmpty()) {
            log.error("Не удалось создать задачу на загрузку. DownloadRequestDto: {}",
                slskDownloadRequestDto);
            return null;
        }

        TrackEntity track = getTrackFromDbOrItunes(slskDownloadRequestDto.trackId());
        if (track == null) {
            log.error("Трек не найден ни в БД ни в ITunes. DownloadRequestDto: {}",
                slskDownloadRequestDto);
            return null;
        }

        UUID uuid = UUID.randomUUID();
        SlskDownloadEntity slskDownloadEntity = SlskDownloadEntity.builder()
            .uuid(uuid)
            // TODO: add auth
            .user(userRepository.getReferenceById(1L))
            .transferId(UUID.fromString(transferDto.get().getId()))
            .trackMetadata(track)
            .build();
        slskDownloadRepository.save(slskDownloadEntity);

        return slskDownloadEntity;
    }

    private TrackEntity getTrackFromDbOrItunes(long trackId) {
        TrackEntity track = trackRepository.findByiTunesId(trackId);
        if (track != null) {
            return track;
        }

        log.error("Для запрошенного трека {} не найдено данных. Загружаем из ITunes", trackId);
        track = itunesSearchService.searchTrackById(trackId);

        if (track != null) {
            track.setTrackStatus(TrackFileStatus.NOT_DOWNLOADED);
            trackRepository.save(track);
            return track;
        }

        log.error("Трека с id {} не найдено", trackId);
        return null;
    }

    public List<SlskDownloadResponseDto> getDownloads(UserEntity user) {
        List<SlskDownloadEntity> userDownloads = slskDownloadRepository.findAllByUser(user);

        return userDownloads.stream()
            .map(this::mapToResponseDto)
            .toList();
    }

    public SlskFileTransferDto getDownloadInfo(UUID downloadUuid, UserEntity user) {
        SlskDownloadEntity downloadEntity = slskDownloadRepository.findByUserAndUuid(user,
            downloadUuid);

        if (downloadEntity == null) {
            log.error("Failed to get download task info. TransferId: {}, User: {}", downloadUuid,
                user);
            return null;
        }

        Optional<SlskFileTransferDto> transferDto = restService.getDownloadByTransferId(
            downloadEntity.getTransferId().toString());

        if (transferDto.isEmpty()) {
            log.error("Failed to get download task info. TransferId: {}, User: {}", downloadUuid,
                user);
            return null;
        }

        return transferDto.get();
    }

    private SlskDownloadResponseDto mapToResponseDto(SlskDownloadEntity downloadEntity) {
        Optional<SlskFileTransferDto> responseDto = restService.getDownloadByTransferId(
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
