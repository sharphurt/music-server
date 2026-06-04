package ru.sharphurt.musicserver.soulseek.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekFileNodeDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekFileScoreDto;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SoulseekScoringService {

    public List<SoulseekFileScoreDto> matchAndSort(TrackDto trackDto, Collection<SoulseekFileNodeDto> slskResults) {
        log.debug("Starting matchTracks for '{}' by '{}' ({} ms), candidates: {}",
                trackDto.getTitle(), trackDto.getArtistName(), trackDto.getDuration(), slskResults.size());

        return slskResults.parallelStream()
                .map(e -> scoreFile(e, trackDto))
                .sorted(Comparator.comparingDouble(SoulseekFileScoreDto::similarityScore).thenComparing(e -> e.fileNodeDto().getUploadSpeed()).reversed())
                .collect(Collectors.toList());
    }

    private SoulseekFileScoreDto scoreFile(SoulseekFileNodeDto fileNodeDto, TrackDto dbTrack) {
        double targetDurationSec = dbTrack.getDuration() / 1000.0;
        double similarityScore = targetDurationSec - Math.abs(targetDurationSec - fileNodeDto.getLength());

        String filename = fileNodeDto.getFilename().toLowerCase();
        String basename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1);

        boolean titleMatched = dbTrack.getTitleAliases().stream()
                .map(String::toLowerCase)
                .anyMatch(basename::contains);

        if (!titleMatched) {
            log.info("Basename {} (Filename {}) has no aliases {}", basename, filename, dbTrack.getTitleAliases());
            similarityScore = 0;
        }

        return new SoulseekFileScoreDto(fileNodeDto, similarityScore);
    }
}