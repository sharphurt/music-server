package ru.sharphurt.musicserver.api.library.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AddToLibraryRequestDto {

    private UUID downloadUuid;

}
