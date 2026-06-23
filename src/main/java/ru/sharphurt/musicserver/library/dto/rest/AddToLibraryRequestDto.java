package ru.sharphurt.musicserver.library.dto.rest;

import java.util.UUID;
import lombok.Data;

@Data
public class AddToLibraryRequestDto {

    private UUID downloadUuid;
}
