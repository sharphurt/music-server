package ru.sharphurt.musicserver.soulseek.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.library.enitiy.TrackEntity;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileNodeDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileScoreDto;

@Slf4j
@Service
public class SlskScoringService {

    public List<SlskFileScoreDto> matchAndSort(TrackEntity trackDto,
        Collection<SlskFileNodeDto> slskResults) {
        log.debug("Starting matchTracks for '{}' by '{}' ({} ms), candidates: {}",
            trackDto.getTitle(), trackDto.getArtistName(), trackDto.getDuration(),
            slskResults.size());

        return slskResults.parallelStream()
            .map(e -> scoreFile(e, trackDto))
            .sorted(Comparator.comparingDouble(SlskFileScoreDto::similarityScore)
                .thenComparing(e -> e.fileNodeDto().getUploadSpeed()).reversed())
            .filter(e -> e.similarityScore() > 0)
            .toList();
    }

    private SlskFileScoreDto scoreFile(SlskFileNodeDto fileNodeDto, TrackEntity dbTrack) {
        double targetDurationSec = dbTrack.getDuration() / 1000.0;
        double diff = Math.abs(targetDurationSec - fileNodeDto.getLength());

        String filename = fileNodeDto.getFilename().toLowerCase();
        String basename = filename.substring(
            Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1);

        boolean titleMatched = dbTrack.getTitleAliases().stream()
            .map(String::toLowerCase)
            .anyMatch(basename::contains);

        if (!titleMatched) {
            return new SlskFileScoreDto(fileNodeDto, 0.0);
        }

        double similarityScore = Math.max(0.0, (1.0 - diff / targetDurationSec)) * 100.0;
        return new SlskFileScoreDto(fileNodeDto, similarityScore);
    }
}