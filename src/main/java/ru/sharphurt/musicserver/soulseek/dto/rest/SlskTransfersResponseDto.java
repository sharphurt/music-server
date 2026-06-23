package ru.sharphurt.musicserver.soulseek.dto.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import ru.sharphurt.musicserver.soulseek.dto.SlskTransferDto;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlskTransfersResponseDto {

    private List<SlskTransferDto> enqueued;
    private List<SlskTransferDto> failed;
}
