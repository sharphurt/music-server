package ru.sharphurt.musicserver.soulseek.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransfersResponseDto {

    private List<TransferDto> enqueued;
    private List<TransferDto> failed;
}
