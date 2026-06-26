package ru.sharphurt.musicserver.library.tempclean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sharphurt.musicserver.common.PathResolver;
import ru.sharphurt.musicserver.common.entity.SlskDownloadEntity;
import ru.sharphurt.musicserver.common.repository.SlskDownloadRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanTempFilesJob {

    private final PathResolver pathResolver;

    private final SlskDownloadRepository downloadRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void cleanCopiedTempFiles() {
        List<SlskDownloadEntity> savedInLibrary = downloadRepository.findAllAddedToLibrary();
        log.info("Очистка {} временных файлов, уже скопированных в библиотеку", savedInLibrary.size());

        downloadRepository.saveAll(savedInLibrary.stream().map(this::removeTempFile).toList());

        LocalDateTime thresholdDate = LocalDateTime.now().minusHours(1);
        List<SlskDownloadEntity> failedLegacy = downloadRepository.findAllFailedLegacy(thresholdDate);
        log.info("Очистка {} временных файлов, загрузка которых была сломана. Выбор записей раньше {}", failedLegacy.size(), thresholdDate);

        downloadRepository.saveAll(failedLegacy.stream().map(this::removeTempFile).toList());

        log.info("Очистка завершена");
    }

    public SlskDownloadEntity removeTempFile(SlskDownloadEntity downloadEntity) {
        Path path = pathResolver.resolveTempFullPath(downloadEntity);
        try {
            Files.delete(path);
            log.info("Успешно удален файл {}", path);

            downloadEntity.setTempCleaned(true);
            downloadEntity.setLocalFilename(null);
            downloadEntity.setLastCleanErrorMessage(null);

        } catch (IOException e) {
            log.error("Не удалось удалить файл {}", path, e);
            downloadEntity.setTempCleaned(false);
            downloadEntity.setLastCleanErrorMessage(e.getMessage());
        }

        return downloadEntity;
    }
}
