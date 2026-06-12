package ru.sharphurt.musicserver.soulseek.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileNodeDto;
import ru.sharphurt.musicserver.soulseek.dto.SlskFileScoreDto;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SlskScoringService {

    public List<SlskFileScoreDto> matchAndSort(TrackDto trackDto,
        Collection<SlskFileNodeDto> slskResults) {
        log.debug("Starting matchTracks for '{}' by '{}' ({} ms), candidates: {}",
            trackDto.getTitle(), trackDto.getArtistName(), trackDto.getDuration(),
            slskResults.size());

        return slskResults.parallelStream()
            .map(e -> scoreFile(e, trackDto))
            .sorted(Comparator.comparingDouble(SlskFileScoreDto::similarityScore)
                .thenComparing(e -> e.fileNodeDto().getUploadSpeed()).reversed())
            .collect(Collectors.toList());
    }

    private SlskFileScoreDto scoreFile(SlskFileNodeDto fileNodeDto, TrackDto dbTrack) {
        double targetDurationSec = dbTrack.getDuration() / 1000.0;
        double diff = Math.abs(targetDurationSec - fileNodeDto.getLength());

        String filename = fileNodeDto.getFilename().toLowerCase();
        String basename = filename.substring(
            Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1);

        boolean titleMatched = dbTrack.getTitleAliases().stream()
            .map(String::toLowerCase)
            .anyMatch(basename::contains);

        if (!titleMatched) {
//            log.info("Basename {} (Filename {}) has no aliases {}", basename, filename,
//                dbTrack.getTitleAliases());
            return new SlskFileScoreDto(fileNodeDto, 0.0);
        }

        double similarityScore = Math.max(0.0, (1.0 - diff / targetDurationSec)) * 100.0;
        return new SlskFileScoreDto(fileNodeDto, similarityScore);
    }
}