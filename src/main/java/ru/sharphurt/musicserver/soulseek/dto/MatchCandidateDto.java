package ru.sharphurt.musicserver.soulseek.dto;

public record MatchCandidateDto(
        String username,
        String filename,
        double similarityScore,
        int length,
        double kbps,
        double sizeMb
) {
}
