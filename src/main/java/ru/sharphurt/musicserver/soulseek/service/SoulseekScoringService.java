package ru.sharphurt.musicserver.soulseek.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.soulseek.dto.MatchCandidateDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekFileNodeDto;
import ru.sharphurt.musicserver.util.DataClearingUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SoulseekScoringService {
    private static final int TIME_TOLERANCE_SEC = 4;
    private static final double SIMILARITY_THRESHOLD = 0.55;

    public List<MatchCandidateDto> matchAndSort(TrackDto dbTrack, Collection<SoulseekFileNodeDto> slskResults) {
        log.debug("Starting matchTracks for track: '{}' by '{}' (duration: {} ms), candidates: {}",
                dbTrack.getTitle(), dbTrack.getArtistName(), dbTrack.getDuration(), slskResults.size());

        List<MatchCandidateDto> bestMatches = new ArrayList<>();
        double targetDurationSec = dbTrack.getDuration() / 1000.0;

        Set<String> targetVariations = buildTargetVariations(dbTrack);
        log.debug("Built {} target variations for track '{}'", targetVariations.size(), dbTrack.getTitle());

        int skippedByDuration = 0;
        int skippedByThreshold = 0;

        for (SoulseekFileNodeDto file : slskResults) {
            double durationDiff = Math.abs(targetDurationSec - file.getLength());
            if (durationDiff > TIME_TOLERANCE_SEC) {
                log.trace("Skipping '{}' — duration mismatch: expected ~{} s, got {} s (diff: {} s)",
                        file.getFilename(), targetDurationSec, file.getLength(), durationDiff);
                skippedByDuration++;
                continue;
            }

            String cleanSlskName = DataClearingUtils.normalizeString(file.getFilename());

            double bestTextSimilarity = 0.0;
            for (String target : targetVariations) {
                double substringScore = bestSubstringSimilarity(target, cleanSlskName);
                double tokenScore = tokenCoverage(target, cleanSlskName);

                double combined = 0.4 * substringScore + 0.6 * tokenScore;
                if (combined > bestTextSimilarity) bestTextSimilarity = combined;
            }

            if (bestTextSimilarity < SIMILARITY_THRESHOLD) {
                log.trace("Skipping '{}' — similarity {} below threshold {}",
                        file.getFilename(), bestTextSimilarity, SIMILARITY_THRESHOLD);
                skippedByThreshold++;
                continue;
            }

            QualityEvaluation eval = evaluateQuality(
                    file.getFilename(),
                    file.getExtension(),
                    file.getSize(),
                    file.getLength()
            );

            double finalScore = bestTextSimilarity + eval.bonusScore();
            int calculatedKbps = (file.getLength() > 0)
                    ? (int) ((file.getSize() * 8) / (file.getLength() * 1000))
                    : 0;
            double sizeMb = file.getSize() / (1024.0 * 1024.0);

            log.debug("Match accepted: '{}' from '{}' — similarity: {}, finalScore: {}, {} kbps, {} MB, fakeFlac: {}",
                    file.getFilename(), file.getUsername(),
                    bestTextSimilarity, finalScore, calculatedKbps, sizeMb, eval.isFakeFlac());

            bestMatches.add(new MatchCandidateDto(
                    file.getUsername(),
                    file.getFilename(),
                    Math.round(bestTextSimilarity * 1000.0) / 1000.0,
                    Math.round(finalScore * 1000.0) / 1000.0,
                    calculatedKbps,
                    eval.isFakeFlac(),
                    Math.round(sizeMb * 100.0) / 100.0
            ));
        }

        bestMatches.sort(Comparator.comparingDouble(MatchCandidateDto::finalScore).reversed());

        log.info("matchTracks done for '{}': {} matches found, {} skipped by duration, {} skipped by threshold",
                dbTrack.getTitle(), bestMatches.size(), skippedByDuration, skippedByThreshold);

        return bestMatches;
    }

    private Set<String> buildTargetVariations(TrackDto dbTrack) {
        Set<String> artists = dbTrack.getArtistNameAliases();
        artists.add(dbTrack.getArtistName());

        Set<String> titles = dbTrack.getTitleAliases();
        titles.add(dbTrack.getTitle());

        Set<String> variations = artists.stream()
                .flatMap(artist -> titles.stream().map(title -> artist + " " + title))
                .map(DataClearingUtils::normalizeString)
                .collect(Collectors.toSet());

        log.trace("Target variations for '{}': {}", dbTrack.getTitle(), variations);
        return variations;
    }

    private QualityEvaluation evaluateQuality(String filename, String ext, long sizeBytes, int durationSec) {
        log.trace("Evaluating quality: file='{}', ext='{}', size={} bytes, duration={} s",
                filename, ext, sizeBytes, durationSec);

        if (durationSec <= 0 || sizeBytes == 0) {
            log.warn("Invalid file data for quality evaluation: '{}' (duration={}, size={})",
                    filename, durationSec, sizeBytes);
            return new QualityEvaluation(-0.5, false);
        }

        double calculatedKbps = (sizeBytes * 8.0) / (durationSec * 1000.0);
        String nameLower = filename.toLowerCase();
        boolean isFlacClaimed = nameLower.contains("flac") || ".flac".equalsIgnoreCase(ext);

        log.trace("'{}': calculatedKbps={}, isFlacClaimed={}", filename, calculatedKbps, isFlacClaimed);

        if (isFlacClaimed && calculatedKbps < 500) {
            log.debug("Fake FLAC detected: '{}' ({} kbps)", filename, calculatedKbps);
            return new QualityEvaluation(-0.4, true);
        }

        if (isFlacClaimed && calculatedKbps >= 600) {
            double bonus = (calculatedKbps > 1400) ? 0.25 : 0.2;
            log.debug("True FLAC: '{}' ({} kbps), bonus={}", filename, calculatedKbps, bonus);
            return new QualityEvaluation(bonus, false);
        }

        if (calculatedKbps >= 310 && calculatedKbps <= 330) {
            log.trace("Lossy high quality: '{}' ({} kbps), bonus=0.1", filename, calculatedKbps);
            return new QualityEvaluation(0.1, false);
        } else if (calculatedKbps >= 240 && calculatedKbps < 310) {
            log.trace("Lossy mid quality: '{}' ({} kbps), bonus=0.05", filename, calculatedKbps);
            return new QualityEvaluation(0.05, false);
        } else if (calculatedKbps < 192) {
            log.debug("Low bitrate penalized: '{}' ({} kbps), bonus=-0.2", filename, calculatedKbps);
            return new QualityEvaluation(-0.2, false);
        }

        log.trace("No quality bonus for: '{}' ({} kbps)", filename, calculatedKbps);
        return new QualityEvaluation(0.0, false);
    }

    private double calculateSimilarityRatio(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        if (s1.isEmpty() && s2.isEmpty()) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        double ratio = (double) (maxLength - distance) / maxLength;

        log.trace("Similarity '{}' vs '{}': distance={}, ratio={}", s1, s2, distance, ratio);
        return ratio;
    }

    private double bestSubstringSimilarity(String target, String text) {
        target = target.toLowerCase();
        text = text.toLowerCase();

        if (target.length() >= text.length()) {
            return calculateSimilarityRatio(target, text);
        }

        int winLen = target.length();
        double best = 0.0;

        int minWin = (int) (winLen * 0.7);
        int maxWin = (int) (winLen * 1.3);

        for (int w = minWin; w <= Math.min(maxWin, text.length()); w++) {
            for (int start = 0; start <= text.length() - w; start++) {
                String window = text.substring(start, start + w);
                double sim = calculateSimilarityRatio(target, window);
                if (sim > best) best = sim;
            }
        }
        return best;
    }

    private double tokenCoverage(String target, String text) {
        Set<String> targetTokens = tokenize(target);
        Set<String> textTokens = tokenize(text);

        if (targetTokens.isEmpty()) return 0.0;

        long matched = targetTokens.stream()
                .filter(t -> textTokens.stream()
                        .anyMatch(tt -> calculateSimilarityRatio(t, tt) >= 0.85))
                .count();

        return (double) matched / targetTokens.size();
    }

    private Set<String> tokenize(String s) {
        return Arrays.stream(s.toLowerCase().split("[\\s\\-_/]+"))
                .filter(t -> !t.isBlank())
                .collect(Collectors.toSet());
    }

    private int levenshteinDistance(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    record QualityEvaluation(double bonusScore, boolean isFakeFlac) {
    }
}