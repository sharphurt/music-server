package ru.sharphurt.musicserver.soulseek.service;

import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.soulseek.dto.MatchCandidate;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekFileNodeDto;
import ru.sharphurt.musicserver.util.DataClearingUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SoulseekScoringService {

    private final int TIME_TOLERANCE_SEC = 4;

    private final double SIMILARITY_THRESHOLD = 0.55;

    public List<MatchCandidate> matchTracks(TrackDto dbTrack, List<SoulseekFileNodeDto> slskResults) {
        List<MatchCandidate> bestMatches = new ArrayList<>();
        double targetDurationSec = dbTrack.getDuration() / 1000.0;

        Set<String> targetVariations = buildTargetVariations(dbTrack);

        for (SoulseekFileNodeDto file : slskResults) {
            if (Math.abs(targetDurationSec - file.getLength()) > TIME_TOLERANCE_SEC) {
                continue;
            }

            String cleanSlskName = DataClearingUtils.normalizeString(file.getFilename());

            double bestTextSimilarity = 0.0;
            for (String target : targetVariations) {
                double similarity = calculateSimilarityRatio(target, cleanSlskName);
                if (similarity > bestTextSimilarity) {
                    bestTextSimilarity = similarity;
                }
            }

            if (bestTextSimilarity >= SIMILARITY_THRESHOLD) {
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

                bestMatches.add(new MatchCandidate(
                        file.getUsername(),
                        file.getFilename(),
                        Math.round(bestTextSimilarity * 1000.0) / 1000.0,
                        Math.round(finalScore * 1000.0) / 1000.0,
                        calculatedKbps,
                        eval.isFakeFlac(),
                        Math.round(sizeMb * 100.0) / 100.0
                ));
            }
        }

        bestMatches.sort(Comparator.comparingDouble(MatchCandidate::finalScore).reversed());
        return bestMatches;
    }

    private Set<String> buildTargetVariations(TrackDto dbTrack) {
        Set<String> artists = dbTrack.getArtistNameAliases();
        artists.add(dbTrack.getArtistName());

        Set<String> titles = dbTrack.getTitleAliases();
        titles.add(dbTrack.getTitle());

        return artists.stream()
                .flatMap(artist -> titles.stream().map(title -> artist + " " + title))
                .map(DataClearingUtils::normalizeString)
                .collect(Collectors.toSet());
    }

    private QualityEvaluation evaluateQuality(String filename, String ext, long sizeBytes, int durationSec) {
        if (durationSec <= 0 || sizeBytes == 0) {
            return new QualityEvaluation(-0.5, false);
        }

        double calculatedKbps = (sizeBytes * 8.0) / (durationSec * 1000.0);
        String nameLower = filename.toLowerCase();
        boolean isFlacClaimed = nameLower.contains("flac") || ".flac".equalsIgnoreCase(ext);

        // Детект Fake FLAC
        if (isFlacClaimed && calculatedKbps < 500) {
            return new QualityEvaluation(-0.4, true);
        }

        // Поощрение за True FLAC
        if (isFlacClaimed && calculatedKbps >= 600) {
            double bonus = (calculatedKbps > 1400) ? 0.25 : 0.2;
            return new QualityEvaluation(bonus, false);
        }

        // Оценка Lossy форматов
        if (calculatedKbps >= 310 && calculatedKbps <= 330) {
            return new QualityEvaluation(0.1, false);
        } else if (calculatedKbps >= 240 && calculatedKbps < 310) {
            return new QualityEvaluation(0.05, false);
        } else if (calculatedKbps < 192) {
            return new QualityEvaluation(-0.2, false);
        }

        return new QualityEvaluation(0.0, false);
    }

    private double calculateSimilarityRatio(String s1, String s2) {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());

        return (double) (maxLength - distance) / maxLength;
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
