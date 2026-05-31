package ru.sharphurt.musicserver.search.musicbrainz;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MusicBrainzQueryBuilder {

    public String buildLuceneQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return "";
        }

        String[] tokens = rawQuery.split("\\s+");
        List<String> termGroups = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            String cleanToken = tokens[i].replaceAll("[^a-zA-Z0-9а-яА-ЯёЁ]", "").toLowerCase();
            if (cleanToken.isEmpty()) {
                continue;
            }

            boolean isLastToken = (i == tokens.length - 1);
            String searchTerm = isLastToken ? cleanToken + "*" : cleanToken;

            String group = "(recording:%s OR artist:%s)".formatted(searchTerm, searchTerm);
            termGroups.add(group);
        }

        return String.join(" AND ", termGroups);
    }
}