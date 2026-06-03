package ru.sharphurt.musicserver.soulseek.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.soulseek.dto.MatchCandidateDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekFileNodeDto;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SoulseekScoringService {

    public List<MatchCandidateDto> matchAndSort(TrackDto trackDto, Collection<SoulseekFileNodeDto> slskResults) {
        log.debug("Starting matchTracks for '{}' by '{}' ({} ms), candidates: {}",
                trackDto.getTitle(), trackDto.getArtistName(), trackDto.getDuration(), slskResults.size());

        return slskResults.parallelStream()
                .map(e -> scoreFile(e, trackDto))
                .sorted(Comparator.comparingDouble(MatchCandidateDto::similarityScore).thenComparing(MatchCandidateDto::sizeMb).reversed())
                .collect(Collectors.toList());
    }

    private MatchCandidateDto scoreFile(SoulseekFileNodeDto fileNodeDto, TrackDto dbTrack) {
        double targetDurationSec = dbTrack.getDuration() / 1000.0;

        double fileSizeMb = fileNodeDto.getSize() / (1024.0 * 1024.0);
        int kbps = fileNodeDto.getLength() > 0
                ? (int) ((fileNodeDto.getSize() * 8L) / (fileNodeDto.getLength() * 1000L)) : 0;

        double similarityScore = targetDurationSec - Math.abs(targetDurationSec - fileNodeDto.getLength());

        return new MatchCandidateDto(
                fileNodeDto.getUsername(),
                fileNodeDto.getFilename(),
                similarityScore,
                fileNodeDto.getLength(),
                kbps,
                fileSizeMb);
    }
}