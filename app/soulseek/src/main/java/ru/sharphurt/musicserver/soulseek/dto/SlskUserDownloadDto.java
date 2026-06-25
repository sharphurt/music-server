package ru.sharphurt.musicserver.soulseek.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlskUserDownloadDto {

    private String username;
    private List<SlskDirectoryDto> directories;
}
