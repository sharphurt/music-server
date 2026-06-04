package ru.sharphurt.musicserver.soulseek.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekFileNodeDto;
import ru.sharphurt.musicserver.soulseek.dto.TransfersResponseDto;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SoulseekDownloadService {

    private final SoulseekRestService restService;

    public TransfersResponseDto enqueueDownload(SoulseekFileNodeDto fileNodeDto) {
        Optional<TransfersResponseDto> responseDto = restService.postDownloadTask(fileNodeDto);

        if (responseDto.isEmpty()) {
            log.error("Failed to enqueue download task. fileNodeDto: {}", fileNodeDto);
        }

        return responseDto.get();
    }

}
