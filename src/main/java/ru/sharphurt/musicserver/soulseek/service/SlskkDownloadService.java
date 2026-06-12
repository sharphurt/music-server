package ru.sharphurt.musicserver.soulseek.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.soulseek.dto.SlskDownloadRequestDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileTransferDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskTransfersResponseDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskUserDownloadDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlskkDownloadService {

    private final SlskRestService restService;

    public SlskTransfersResponseDto enqueueDownload(SlskDownloadRequestDto slskDownloadRequestDto) {
        Optional<SlskTransfersResponseDto> responseDto = restService.postDownloadTask(
            slskDownloadRequestDto.fileName(),
            slskDownloadRequestDto.userName(),
            slskDownloadRequestDto.size());

        if (responseDto.isEmpty()) {
            log.error("Failed to enqueue download task. DownloadRequestDto: {}",
                slskDownloadRequestDto);
            return null;
        }

        return responseDto.get();
    }

    public List<SlskFileTransferDto> getDownloads() {
        List<SlskUserDownloadDto> downloads = restService.getDownloads();

        return downloads.stream()
            .flatMap(userDownloadDto -> userDownloadDto
                .getDirectories()
                .stream()
                .flatMap(slskDirectoryDto -> slskDirectoryDto
                    .getFiles()
                    .stream()))
            .sorted(
                Comparator.comparing(
                    SlskFileTransferDto::getEnqueuedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())
                )
            )
            .toList();
    }


    public SlskFileTransferDto getDownloadInfo(String transferId) {
        Optional<SlskFileTransferDto> downloadInfo = restService.getDownloadById(transferId);

        if (downloadInfo.isEmpty()) {
            log.error("Failed to get download task info. TransferId: {}", transferId);
            return null;
        }

        return downloadInfo.get();
    }
}
