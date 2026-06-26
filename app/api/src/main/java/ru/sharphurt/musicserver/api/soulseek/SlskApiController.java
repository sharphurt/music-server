package ru.sharphurt.musicserver.api.soulseek;


import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.sharphurt.musicserver.api.soulseek.dto.SlskSearchResponseDto;
import ru.sharphurt.musicserver.common.PathResolver;
import ru.sharphurt.musicserver.common.entity.SlskDownloadEntity;
import ru.sharphurt.musicserver.common.entity.SlskSearchTaskEntity;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.common.entity.TrackFileStatus;
import ru.sharphurt.musicserver.common.repository.UserRepository;
import ru.sharphurt.musicserver.mediametadata.db.MetadataService;
import ru.sharphurt.musicserver.soulseek.dto.SlskDownloadRequestDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskDownloadResponseDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileScoreDto;
import ru.sharphurt.musicserver.soulseek.service.SlskDownloadService;
import ru.sharphurt.musicserver.soulseek.service.SlskSearchService;

@Slf4j
@RestController
@RequestMapping("/api/soulseek")
@RequiredArgsConstructor
public class SlskApiController {

    private final SlskSearchService searchService;

    private final SlskDownloadService downloadService;

    private final UserRepository userRepository;

    private final MetadataService mediaMetadataService;

    private final PathResolver pathResolver;

    @PostMapping("/search")
    public ResponseEntity<SlskSearchResponseDto> createSearchTask(@RequestParam Long trackId) {
        TrackEntity trackData = mediaMetadataService.findOrFetchTrack(trackId);

        if (trackData == null) {
            log.error("Не известный TrackId = {}", trackId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        SlskSearchResponseDto responseDto = SlskSearchResponseDto.builder()
            .trackData(trackData)
            .build();

        if (trackData.getTrackStatus() == TrackFileStatus.NOT_DOWNLOADED) {
            List<SlskSearchTaskEntity> searchTasks = searchService.initiateSearch(trackId);
            responseDto.setCreatedTasks(searchTasks);
            return ResponseEntity.ok(responseDto);
        }

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/search/results")
    public ResponseEntity<List<SlskFileScoreDto>> getSearchResults(@RequestParam Long trackId, @RequestParam(defaultValue = "200") Integer maxResults) {
        List<SlskFileScoreDto> results = searchService.fetchSearchResults(trackId, maxResults);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/download")
    public ResponseEntity<SlskDownloadEntity> enqueueDownload(@RequestBody SlskDownloadRequestDto downloadRequestDto) {
        SlskDownloadEntity downloadEntity = downloadService.enqueueDownload(
            downloadRequestDto.getTrackId(),
            downloadRequestDto.getFilename(),
            downloadRequestDto.getUsername(),
            downloadRequestDto.getSize(),
            downloadRequestDto.getDownloadIntent());

        if (downloadEntity == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(downloadEntity);
    }

    @GetMapping("/download/list")
    public ResponseEntity<List<SlskDownloadResponseDto>> getDownloads() {
        List<SlskDownloadResponseDto> downloads = downloadService.getDownloads(
            userRepository.getReferenceById(1L));
        if (downloads == null || downloads.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(downloads);
    }

    // TODO REFACTOR
    @GetMapping("/stream")
    public ResponseEntity<StreamingResponseBody> streamAudio(
        @RequestParam("downloadUuid") UUID downloadUuid,
        @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {

        log.info("Requested stream: downloadUuid: {}, rangeHeader: {}", downloadUuid, rangeHeader);
        //TODO add auth
        SlskDownloadEntity downloadInfo = downloadService.getDownloadInfo(downloadUuid,
            userRepository.getReferenceById(1L));

        if (downloadInfo == null) {
            log.error("Загрузка, для которой запрошен стрим, не найдена. DownloadUuid: {}",
                downloadUuid);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Path resolvedPath = pathResolver.resolveTempFullPath(downloadInfo);
        log.info("Resolved path: {}", resolvedPath);

        if (!Files.exists(resolvedPath)) {
            log.info("File not exists in path: {}", resolvedPath);
        }

        File audioFile = resolvedPath.toFile();
        if (!audioFile.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        long totalExpectedSize = downloadInfo.getSlskFilesize();
        long currentPhysicalSize = audioFile.length();

        long rangeStart = 0;
        long rangeEnd = currentPhysicalSize - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            try {
                rangeStart = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    rangeEnd = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (rangeStart >= currentPhysicalSize) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .header(HttpHeaders.CONTENT_RANGE, "bytes */" + totalExpectedSize)
                .build();
        }

        rangeEnd = Math.min(rangeEnd, currentPhysicalSize - 1);
        long contentLength = rangeEnd - rangeStart + 1;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.set(HttpHeaders.CONTENT_RANGE,
            String.format("bytes %d-%d/%d", rangeStart, rangeEnd, totalExpectedSize));
        headers.setContentLength(contentLength);

        String contentType = "audio/mpeg";
        try {
            String probed = Files.probeContentType(resolvedPath);
            if (probed != null) {
                contentType = probed;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        headers.setContentType(MediaType.parseMediaType(contentType));

        StreamingResponseBody responseBody = getStreamingResponseBody(rangeStart, contentLength,
            audioFile);

        return new ResponseEntity<>(responseBody, headers, HttpStatus.PARTIAL_CONTENT);
    }

    private static StreamingResponseBody getStreamingResponseBody(
        long rangeStart,
        long contentLength,
        File audioFile) {

        final long finalRangeStart = rangeStart;
        final long finalContentLength = contentLength;
        final File finalAudioFile = audioFile;

        return outputStream -> {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(finalAudioFile,
                "r")) {
                randomAccessFile.seek(finalRangeStart);

                byte[] buffer = new byte[8192];
                long bytesToRead = finalContentLength;
                int bytesRead;

                while (bytesToRead > 0 &&
                    (bytesRead = randomAccessFile.read(buffer, 0,
                        (int) Math.min(buffer.length, bytesToRead))) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    bytesToRead -= bytesRead;
                }

                outputStream.flush();
            }
        };
    }


}
