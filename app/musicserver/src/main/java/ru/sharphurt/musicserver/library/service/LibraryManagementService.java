package ru.sharphurt.musicserver.library.service;

import jakarta.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.common.PathResolver;
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

    private final PathResolver pathResolver;

    @Transactional
    public AddToLibraryResult addToLibrary(UUID downloadUuid, UserEntity user) {
        SlskDownloadEntity downloadInfo = downloadRepository.findByUserAndUuid(user, downloadUuid);
        if (downloadInfo == null) {
            return AddToLibraryResult.NOT_FOUND;
        }

        return switch (downloadInfo.getDownloadStatus()) {
            case IN_LIBRARY -> AddToLibraryResult.ALREADY_IN_LIBRARY;

            case COMPLETED -> {
                Path resolvedPath = pathResolver.resolveTempFullPath(downloadInfo);
                if (Files.exists(resolvedPath)) {
                    log.info("Перенос трека {} в библиотеку", resolvedPath);

                    downloadInfo.setDownloadIntent(DownloadIntent.ADD);
                    libraryFileService.copyToLibrary(downloadInfo);
                    yield AddToLibraryResult.MOVED;
                } else {
                    log.info("Файл {} не найден во временном хранилище. Будет загружен заново", resolvedPath);
                    yield requeue(downloadInfo);
                }
            }

            case MOVING, QUEUED -> {
                downloadInfo.setDownloadIntent(DownloadIntent.ADD);
                downloadRepository.save(downloadInfo);
                yield AddToLibraryResult.REQUEUED;
            }

            case FAILED -> requeue(downloadInfo);
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
