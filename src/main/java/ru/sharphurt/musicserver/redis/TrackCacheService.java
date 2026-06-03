package ru.sharphurt.musicserver.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.dto.TrackDto;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackCacheService {

    private static final String TRACK_METADATA_KEY_TEMPLATE = "itunes:track:%s:metadata";

    private final RedisTemplate<String, TrackDto> redisTemplate;

    public void save(TrackDto trackDto) {
        redisTemplate.opsForValue().set(TRACK_METADATA_KEY_TEMPLATE.formatted(trackDto.getITunesId()), trackDto, Duration.ofHours(24));
    }

    public void saveAll(List<TrackDto> trackDtos) {
        redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            for (TrackDto trackDto : trackDtos) {
                save(trackDto);
            }
            return null;
        });
    }

    public TrackDto get(long trackId) {
        return (TrackDto) redisTemplate.opsForHash().get(TRACK_METADATA_KEY_TEMPLATE, trackId);
    }

    public void delete(long trackId) {
        redisTemplate.delete(TRACK_METADATA_KEY_TEMPLATE.formatted(trackId));
    }
}
