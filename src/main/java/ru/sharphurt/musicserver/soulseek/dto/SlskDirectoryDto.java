package ru.sharphurt.musicserver.soulseek.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlskDirectoryDto {

    private String directory;
    private int fileCount;
    private List<SlskFileTransferDto> files;
}
