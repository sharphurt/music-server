package ru.sharphurt.musicserver.library.service;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.common.PathResolver;
import ru.sharphurt.musicserver.common.entity.DownloadStatus;
import ru.sharphurt.musicserver.common.entity.SlskDownloadEntity;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.common.entity.TrackFileStatus;
import ru.sharphurt.musicserver.common.repository.SlskDownloadRepository;
import ru.sharphurt.musicserver.common.repository.TrackRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryFileService {

    private final SlskDownloadRepository downloadRepository;

    private final TrackRepository trackRepository;

    private final PathResolver pathResolver;

    @Transactional
    public void copyToLibrary(SlskDownloadEntity download) {
        TrackEntity track = download.getTrackMetadata();
        Path source = pathResolver.resolveTempFullPath(download);
        Path target = pathResolver.resolveLibraryFullPath(download);

        download.setDownloadStatus(DownloadStatus.MOVING);
        downloadRepository.save(download);

        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

            track.setFullPath(target.toString());
            track.setTrackStatus(TrackFileStatus.IN_LIBRARY);
            trackRepository.save(track);

            download.setLibraryFilename(target.toString());
            download.setDownloadStatus(DownloadStatus.IN_LIBRARY);
        } catch (IOException e) {
            log.error("Не удалось переместить файл {} → {}", source, target, e);
            download.setDownloadStatus(DownloadStatus.FAILED);
            download.setErrorMessage(e.getMessage());
        }

        downloadRepository.save(download);

        //TODO: add to Recently added playlist
        //TODO: download lyrics
        //TODO: download cover
        //TODO: set all metadata to file
    }

}

