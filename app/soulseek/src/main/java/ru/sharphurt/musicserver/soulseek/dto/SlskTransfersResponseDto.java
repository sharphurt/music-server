package ru.sharphurt.musicserver.soulseek.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlskTransfersResponseDto {

    private List<SlskTransferDto> enqueued;
    private List<SlskTransferDto> failed;
}
