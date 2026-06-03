package ru.sharphurt.musicserver.soulseek.dto;

import java.util.UUID;

public record SoulseekSearchTaskDto(long trackId, String query, UUID uuid) {
}