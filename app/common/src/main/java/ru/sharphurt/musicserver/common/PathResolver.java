package ru.sharphurt.musicserver.common;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.sharphurt.musicserver.common.entity.SlskDownloadEntity;

@Component
public class PathResolver {

    @Value("${slskd.internal-base-path}")
    private String slskdInternalBasePath;

    @Value("${slskd.host-base-path}")
    private String slskdHostBasePath;

    @Value("${library.base-dir}")
    private String libraryRoot;

    public Path getIncompleteTempPath(String remoteFilename) {
        Path relativeFilename = parseRemotePath(remoteFilename);
        return Paths.get(slskdInternalBasePath, "/incomplete", relativeFilename.toString());
    }

    public Path resolveTempFullPath(SlskDownloadEntity downloadEntity) {
        String relative = downloadEntity.getLocalFilename().replaceFirst("^" + Pattern.quote(slskdInternalBasePath), "");
        return Paths.get(slskdHostBasePath, relative);
    }

    public Path resolveLibraryFullPath(SlskDownloadEntity downloadEntity) {
        String artist = sanitize(downloadEntity.getTrackMetadata().getArtist().getArtistName());
        String album = sanitize(downloadEntity.getTrackMetadata().getAlbum().getAlbumName());
        String extension = FilenameUtils.getExtension(downloadEntity.getLocalFilename());
        int trackNumber = downloadEntity.getTrackMetadata().getTrackNumber();
        String title = downloadEntity.getTrackMetadata().getTitle();
        String filename = sanitize("%s - %s.%s".formatted(trackNumber, title, extension));

        return Path.of(libraryRoot, artist, album, filename);
    }

    private String sanitize(String s) {
        return s == null ? "Unknown" : s.replaceAll("[\\\\/:*?\"<>|]", "_");
    }


    private Path parseRemotePath(String remoteFilename) {
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
