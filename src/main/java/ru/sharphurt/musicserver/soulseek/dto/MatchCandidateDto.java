package ru.sharphurt.musicserver.soulseek.dto;

public record MatchCandidateDto(
        String username,
        String filename,
        double similarityScore,
        double finalScore,
        int calculatedKbps,
        boolean isFakeFlac,
        double sizeMb
) {
}
