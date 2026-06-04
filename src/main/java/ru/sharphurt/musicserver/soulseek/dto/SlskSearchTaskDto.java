package ru.sharphurt.musicserver.soulseek.dto;

import java.util.UUID;

public record SlskSearchTaskDto(long trackId, String query, UUID uuid) {

}