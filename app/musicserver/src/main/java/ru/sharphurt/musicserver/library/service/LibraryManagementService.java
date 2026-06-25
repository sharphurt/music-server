package ru.sharphurt.musicserver.library.service;

import jakarta.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.common.entity.DownloadIntent;
import ru.sharphurt.musicserver.common.entity.DownloadStatus;
import ru.sharphurt.musicserver.common.entity.SlskDownloadEntity;
import ru.sharphurt.musicserver.common.entity.UserEntity;
import ru.sharphurt.musicserver.common.repository.SlskDownloadRepository;
import ru.sharphurt.musicserver.library.AddToLibraryResult;
import ru.sharphurt.musicserver.soulseek.service.SlskDownloadService;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryManagementService {

    private final SlskDownloadRepository downloadRepository;

    private final LibraryFileService libraryFileService;

    private final SlskDownloadService slskDownloadService;

    @Transactional
    public AddToLibraryResult addToLibrary(UUID downloadUuid, UserEntity user) {
        SlskDownloadEntity download = downloadRepository.findByUserAndUuid(user, downloadUuid);
        if (download == null) {
            return AddToLibraryResult.NOT_FOUND;
        }

        return switch (download.getDownloadStatus()) {
            case IN_LIBRARY -> AddToLibraryResult.ALREADY_IN_LIBRARY;

            case COMPLETED -> {
                Path file = Path.of(download.getLocalFilename());
                if (Files.exists(file)) {
                    download.setDownloadIntent(DownloadIntent.ADD);
                    libraryFileService.moveToLibrary(download);
                    yield AddToLibraryResult.MOVED;
                } else {
                    yield requeue(download);
                }
            }

            case MOVING, QUEUED -> {
                download.setDownloadIntent(DownloadIntent.ADD);
                downloadRepository.save(download);
                yield AddToLibraryResult.REQUEUED;
            }

            case FAILED -> requeue(download);
        };
    }

    private AddToLibraryResult requeue(SlskDownloadEntity download) {
        download.setDownloadStatus(DownloadStatus.QUEUED);
        download.setDownloadIntent(DownloadIntent.ADD);
        downloadRepository.save(download);

        UUID downloadId = download.getUuid();
        boolean requeueResult = slskDownloadService.requeueWithFallback(downloadId);
        if (requeueResult) {
            log.info("Файл будет заново загружен {}", download);
            return AddToLibraryResult.REQUEUED;
        } else {
            log.error("Не получилось заново загрузить файл {}", download);
            return AddToLibraryResult.NOT_FOUND;
        }
    }
}
