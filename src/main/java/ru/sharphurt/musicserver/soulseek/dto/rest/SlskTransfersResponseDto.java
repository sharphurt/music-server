package ru.sharphurt.musicserver.soulseek.dto.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileTransferDto;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlskTransfersResponseDto {

    private List<SlskFileTransferDto> enqueued;
    private List<SlskFileTransferDto> failed;
}
