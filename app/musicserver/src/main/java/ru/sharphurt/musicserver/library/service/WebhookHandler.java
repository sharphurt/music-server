package ru.sharphurt.musicserver.library.service;

import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.common.entity.DownloadIntent;
import ru.sharphurt.musicserver.common.entity.DownloadStatus;
import ru.sharphurt.musicserver.common.entity.SlskDownloadEntity;
import ru.sharphurt.musicserver.common.repository.SlskDownloadRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookHandler {

    private final SlskDownloadRepository downloadRepository;
    private final LibraryFileService fileMoveService;

    @Transactional
    public void onDownloadComplete(String localFilename, UUID transferId) {
        SlskDownloadEntity download = downloadRepository.findByTransferId(transferId);

        if (download == null) {
            log.warn("Получен webhook для неизвестного transferId: {}", transferId);
            return;
        }

        download.setLocalFilename(localFilename);
        download.setDownloadStatus(DownloadStatus.COMPLETED);

        download = downloadRepository.save(download);

        if (download.getDownloadIntent() == DownloadIntent.ADD) {
            fileMoveService.moveToLibrary(download);
        }
    }
}
