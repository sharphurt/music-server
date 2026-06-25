package ru.sharphurt.musicserver.library.service;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    @Value("${library.base-dir}")
    private String libraryRoot;

    @Value("${slskd.host-base-path}")
    private String soulseekAppDir;

    private final SlskDownloadRepository downloadRepository;

    private final TrackRepository trackRepository;

    @Transactional
    public void moveToLibrary(SlskDownloadEntity download) {
        TrackEntity track = download.getTrackMetadata();
        Path source = Path.of(soulseekAppDir, download.getLocalFilename().replaceAll("/app", ""));
        Path target = resolveLibraryPath(track, source.getFileName().toString());

        download.setDownloadStatus(DownloadStatus.MOVING);
        downloadRepository.save(download);

        try {
            Files.createDirectories(target.getParent());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

            track.setFullPath(target.toString());
            track.setTrackStatus(TrackFileStatus.IN_LIBRARY);
            trackRepository.save(track);

            download.setLibraryFilename(target.toString());
            download.setLocalFilename(target.toString());
            download.setDownloadStatus(DownloadStatus.IN_LIBRARY);
        } catch (IOException e) {
            log.error("Не удалось переместить файл {} → {}", source, target, e);
            download.setDownloadStatus(DownloadStatus.FAILED);
            download.setErrorMessage(e.getMessage());
        }

        downloadRepository.save(download);
    }

    private Path resolveLibraryPath(TrackEntity track, String filename) {
        String artist = sanitize(track.getArtistName());
        String album = sanitize(track.getAlbumName());
        return Path.of(libraryRoot, artist, album, filename);
    }

    private String sanitize(String s) {
        return s == null ? "Unknown" : s.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}

