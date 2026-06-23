package ru.sharphurt.musicserver.library.service;

import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.soulseek.dto.rest.SlskDownloadCompleteDto;
import ru.sharphurt.musicserver.soulseek.entity.DownloadIntent;
import ru.sharphurt.musicserver.soulseek.entity.DownloadStatus;
import ru.sharphurt.musicserver.soulseek.entity.SlskDownloadEntity;
import ru.sharphurt.musicserver.soulseek.repository.SlskDownloadRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookHandler {

    private final SlskDownloadRepository downloadRepository;
    private final LibraryFileMoveService fileMoveService;

    @Transactional
    public void onDownloadComplete(SlskDownloadCompleteDto event) {
        UUID transferId = UUID.fromString(event.getTransfer().getId());
        SlskDownloadEntity download = downloadRepository.findByTransferId(transferId);

        if (download == null) {
            log.warn("Получен webhook для неизвестного transferId: {}", transferId);
            return;
        }

        download.setLocalFilename(event.getLocalFilename());
        download.setDownloadStatus(DownloadStatus.COMPLETED);

        download = downloadRepository.save(download);

        if (download.getDownloadIntent() == DownloadIntent.ADD) {
            fileMoveService.moveToLibrary(download);
        }
    }
}
