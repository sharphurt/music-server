package ru.sharphurt.musicserver.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DataClearingUtils {

    //    private static final Pattern EXTENSION_PATTERN = Pattern.compile("\\.[a-zA-Z0-9]+$");
//    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s+");

    private static final Set<String> STOP_WORDS = Set.of(
        // оригинальные
        "music", "torrent", "complete", "unsorted", "download", "downloads", "new",
        "web", "rip", "kbps", "flac", "mp3", "m4a", "aac", "opus", "ogg", "wav",
        "official", "audio", "video", "single", "ep", "album", "soundtrack", "ost",
        "cd", "vinyl", "lossless", "hires", "bit", "khz", "hz",
        "pmedia", "choko", "feat",

        // дополнительные форматы/кодеки
        "wma", "ape", "alac", "aiff", "aif", "dsf", "dff", "mka", "tak", "tta",

        // битрейты и качество
        "320", "256", "192", "128", "96", "v0", "v2", "cbr", "vbr",
        "24bit", "16bit", "hd", "uhd", "hi-res",

        // источники рипа
        "cdda", "sacd", "dvd", "bluray", "bd", "webrip",
        "hdtracks", "tidal", "qobuz", "deezer", "itunes", "amazon",
        "beatport", "bandcamp", "spotify", "junodownload",

        // теги релиза
        "remaster", "remastered", "remasters", "reissue", "rerelease",
        "deluxe", "edition", "expanded", "anniversary", "limited", "special",
        "retail", "promo", "bonus", "extras",

        // структура релиза
        "disc", "disk", "vol", "volume", "track", "side",

        // служебные файлы, попавшие в имя
        "nfo", "sfv", "m3u", "cue", "log",

        // мусор из папок
        "inbox", "incoming", "shared", "temp", "library", "collection",
        "archive", "folder", "misc", "various",

        // прочие частые шумы
        "full", "split", "set", "fix", "proper", "int", "intl"
    );

    public static String normalizeString(String filename) {
        if (filename == null || filename.isEmpty()) {
            log.warn("Try to clear null or empty string");
            return "";
        }

        String[] tokens = MULTIPLE_SPACES_PATTERN.split(filename.trim());
        log.trace("\tSplitted to tokens: [{}]", String.join(", ", tokens));
        List<String> filtered = Arrays.stream(tokens)
            .filter(t -> !t.isEmpty())
            .filter(t -> !STOP_WORDS.contains(t))
            .toList();
        log.trace("\tFiltered tokens: [{}]", String.join(", ", filtered));

        return String.join(" ", filtered);
    }
}
