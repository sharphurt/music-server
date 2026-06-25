package ru.sharphurt.musicserver.soulseek.service;

import jakarta.transaction.Transactional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.common.entity.DownloadIntent;
import ru.sharphurt.musicserver.common.entity.DownloadStatus;
import ru.sharphurt.musicserver.common.entity.SlskDownloadEntity;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.common.entity.UserEntity;
import ru.sharphurt.musicserver.common.repository.SlskDownloadRepository;
import ru.sharphurt.musicserver.common.repository.UserRepository;
import ru.sharphurt.musicserver.mediametadata.db.MetadataService;
import ru.sharphurt.musicserver.soulseek.dto.SlskDownloadResponseDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskTransferDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskTransfersResponseDto;
import ru.sharphurt.musicserver.soulseek.mapper.SlskDownloadDtoMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlskDownloadService {

    private final SlskRestService restService;
    private final SlskDownloadRepository slskDownloadRepository;
    private final MetadataService mediaMetadataService;
    private final UserRepository userRepository;
    private final SlskDownloadDtoMapper mapper;

    @Transactional
    public SlskDownloadEntity enqueueDownload(long trackId, String filename, String username, long size, DownloadIntent intent) {
        SlskDownloadEntity existingDownload = slskDownloadRepository
            .findByUserAndSlskUsernameAndSlskFilenameAndDownloadStatusNotIn(
                userRepository.getReferenceById(1L),
                username,
                filename,
                List.of(DownloadStatus.FAILED, DownloadStatus.IN_LIBRARY)
            );

        if (existingDownload != null) {
            updateIntentIfNecessary(intent, existingDownload);
            return existingDownload;
        }

        SlskTransfersResponseDto responseDto = restService.postDownloadTask(filename, username, size);

        if (responseDto == null) {
            return null;
        }

        Optional<SlskTransferDto> transferDto = responseDto.getEnqueued()
            .stream()
            .filter(e -> Objects.equals(e.getFilename(), filename) && Objects.equals(e.getUsername(), username))
            .findFirst();

        if (transferDto.isEmpty()) {
            log.error("Файл {} пользователя {} не был добавлен в очередь", filename, username);
            return null;
        }

        TrackEntity track = mediaMetadataService.findOrFetchTrack(trackId);

        // TODO: решить красиво проблему резолва путей
        Path relativeFilename = parseRelativePath(filename);
        Path incompleteLocalPath = Paths.get("\\incomplete", relativeFilename.toString());

        log.info("Incomplete local path: {}", incompleteLocalPath);

        SlskDownloadEntity entity = SlskDownloadEntity.builder()
            .user(userRepository.getReferenceById(1L)) // TODO: add auth
            .transferId(UUID.fromString(transferDto.get().getId()))
            .trackMetadata(track)
            .downloadIntent(intent != null ? intent : DownloadIntent.PLAY)
            .requestedAt(transferDto.get().getRequestedAt())
            .downloadStatus(DownloadStatus.QUEUED)
            .localFilename(incompleteLocalPath.toString())
            .slskUsername(username)
            .slskFilename(filename)
            .slskFilesize(size)
            .build();

        slskDownloadRepository.save(entity);

        return entity;
    }

    private void updateIntentIfNecessary(DownloadIntent intent, SlskDownloadEntity downloadEntity) {
        if (intent.getPriority() <= downloadEntity.getDownloadIntent().getPriority()) {
            log.debug("Обновление Intent не происходит, запрошена {} менее приоритетная, чем {} или равна. SlskDownloadEntity UUID: {}",
                intent.name(),
                downloadEntity.getDownloadIntent().name(),
                downloadEntity.getUuid());

            return;
        }

        downloadEntity.setDownloadIntent(intent);
        log.info("Установлен Intent {} для SlskDownloadEntity UUID: {}", intent, downloadEntity.getUuid());
        slskDownloadRepository.save(downloadEntity);
    }

    @Transactional
    public boolean requeueWithFallback(UUID downloadId) {
        SlskDownloadEntity download = slskDownloadRepository.findById(downloadId).orElseThrow();
        TrackEntity track = download.getTrackMetadata();

        SlskDownloadEntity result = enqueueDownload(
            track.getITunesId(),
            download.getSlskFilename(),
            download.getSlskUsername(),
            download.getSlskFilesize(),
            DownloadIntent.ADD);

        if (result == null) {
            log.error("Не удалось повторно загрузить файл. SlskDownloadEntity: {}", download);
            return false;
        }

        return true;
    }

    @Transactional
    public List<SlskDownloadResponseDto> getDownloads(UserEntity user) {
        List<SlskDownloadEntity> userDownloads = slskDownloadRepository.findAllByUser(user);

        return userDownloads.stream()
            .map(mapper::mapToResponseDto)
            .toList();
    }

    @Transactional
    public SlskDownloadEntity getDownloadInfo(UUID downloadUuid, UserEntity user) {
        SlskDownloadEntity downloadEntity = slskDownloadRepository.findByUserAndUuid(user,
            downloadUuid);

        if (downloadEntity == null) {
            log.error("Failed to get download task info. TransferId: {}, User: {}", downloadUuid,
                user);
            return null;
        }

        return downloadEntity;
    }

    private Path parseRelativePath(String remoteFilename) {
        String normalized = remoteFilename.replace("\\", "/");
        String[] parts = normalized.split("/");

        if (parts.length >= 2) {
            String parentDir = parts[parts.length - 2];
            String fileName = parts[parts.length - 1];
            return Paths.get(parentDir, fileName);
        }

        return Paths.get(normalized);
    }
}
