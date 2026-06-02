package ru.sharphurt.musicserver.soulseek.dto;

import java.util.UUID;

public record SoulseekSearchTaskDto(String query, UUID uuid) {}