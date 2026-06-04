package ru.sharphurt.musicserver.util;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationDeserializer extends StdDeserializer<Duration> {

    private static final Pattern TIMESPAN_PATTERN = Pattern.compile(
        "^(?:(\\d+)\\.)?([0-1]?\\d|2[0-3]):([0-5]?\\d):([0-5]?\\d)(?:\\.(\\d{1,7}))?$"
    );

    public DurationDeserializer() {
        super(Duration.class);
    }

    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctx) {
        String value = p.getText();
        if (value == null || value.isBlank()) {
            return null;
        }

        Matcher matcher = TIMESPAN_PATTERN.matcher(value.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot parse .NET TimeSpan: " + value);
        }

        long days = matcher.group(1) != null ? Long.parseLong(matcher.group(1)) : 0;
        long hours = Long.parseLong(matcher.group(2));
        long minutes = Long.parseLong(matcher.group(3));
        long seconds = Long.parseLong(matcher.group(4));

        long nanos = 0;
        if (matcher.group(5) != null) {
            String frac = String.format("%-7s", matcher.group(5)).replace(' ', '0');
            nanos = Long.parseLong(frac) * 100L;
        }

        return Duration.ofDays(days)
            .plusHours(hours)
            .plusMinutes(minutes)
            .plusSeconds(seconds)
            .plusNanos(nanos);
    }
}