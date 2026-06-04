package ru.sharphurt.musicserver.soulseek.dto;

public record SoulseekFileScoreDto(
        SoulseekFileNodeDto fileNodeDto,
        double similarityScore
) {
}
