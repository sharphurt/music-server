package ru.sharphurt.musicserver.soulseek.dto;

public record SlskFileScoreDto(
    SlskFileNodeDto fileNodeDto,
    double similarityScore
) {

}
