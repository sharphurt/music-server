package ru.sharphurt.musicserver.soulseek.controller;


import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import ru.sharphurt.musicserver.soulseek.dto.SlskFileScoreDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskTransferDto;
import ru.sharphurt.musicserver.soulseek.dto.rest.SlskDownloadRequestDto;
import ru.sharphurt.musicserver.soulseek.dto.rest.SlskDownloadResponseDto;
import ru.sharphurt.musicserver.soulseek.entity.SlskDownloadEntity;
import ru.sharphurt.musicserver.soulseek.entity.SlskSearchTaskEntity;
import ru.sharphurt.musicserver.soulseek.service.SlskSearchService;
import ru.sharphurt.musicserver.soulseek.service.SlskDownloadService;
import ru.sharphurt.musicserver.user.repository.UserRepository;

@Slf4j
@RestController
@RequestMapping("/api/soulseek")
@RequiredArgsConstructor
public class SlskApiController {

    private final SlskSearchService searchService;

    private final SlskDownloadService downloadService;

    private final UserRepository userRepository;

    @Value("${slskd.complete-dir}")
    private String downloadsDir;

    @Value("${slskd.incomplete-dir}")
    private String incompleteDir;

    @PostMapping("/search")
    public ResponseEntity<List<SlskSearchTaskEntity>> createSearchTask(@RequestParam Long trackId) {
        List<SlskSearchTaskEntity> searchTasks = searchService.initiateSearch(trackId);
        return ResponseEntity.ok(searchTasks);
    }

    @GetMapping("/search/results")
    public ResponseEntity<List<SlskFileScoreDto>> getSearchResults(@RequestParam Long trackId,
        @RequestParam(defaultValue = "200") Integer maxResults) {
        List<SlskFileScoreDto> results = searchService.fetchSearchResults(trackId, maxResults);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/download")
    public ResponseEntity<SlskDownloadEntity> enqueueDownload(
        @RequestBody SlskDownloadRequestDto downloadRequestDto) {
        SlskDownloadEntity downloadEntity = downloadService.enqueueDownload(downloadRequestDto);
        if (downloadEntity == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(downloadEntity);
    }

    @GetMapping("/download/list")
    public ResponseEntity<List<SlskDownloadResponseDto>> getDownloads() {
        List<SlskDownloadResponseDto> downloads = downloadService.getDownloads(userRepository.getReferenceById(1L));
        if (downloads == null || downloads.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(downloads);
    }

    // TODO REFACTOR
    @GetMapping("/stream")
    public ResponseEntity<StreamingResponseBody> streamAudio(
        @RequestParam UUID downloadUuid,
        @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {

        log.info("Requested stream: downloadUuid: {}, rangeHeader: {}", downloadUuid, rangeHeader);
        //TODO add auth
        SlskTransferDto task = downloadService.getDownloadInfo(downloadUuid, userRepository.getReferenceById(1L));
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Path relativePath = parseRelativePath(task.getFilename());

        Path resolvedPath = Paths.get(downloadsDir, relativePath.toString());
        log.info("Resolved path: {}", resolvedPath);

        if (!Files.exists(resolvedPath)) {
            log.info("File not exists in path: {}", resolvedPath);
            resolvedPath = Paths.get(incompleteDir, relativePath.toString());
            log.info("Path will be: {}", resolvedPath);
        }

        File audioFile = resolvedPath.toFile();
        if (!audioFile.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        long totalExpectedSize = task.getSize();
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
