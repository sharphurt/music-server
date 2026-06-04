package ru.sharphurt.musicserver.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.soulseek.dto.SlskSearchTaskDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SoulseekTaskCacheService {

    private static final String SOULSEEK_TASK_KEY_TEMPLATE = "soulseek:search:track:%s";

    private final RedisTemplate<String, SlskSearchTaskDto> redisTemplate;

    public void save(SlskSearchTaskDto taskDto) {
        redisTemplate.opsForHash().put(
                SOULSEEK_TASK_KEY_TEMPLATE.formatted(taskDto.trackId()),
                taskDto.uuid().toString(),
                taskDto);
    }

    public List<SlskSearchTaskDto> getAllForTrack(long trackId) {
        return redisTemplate.<String, SlskSearchTaskDto>opsForHash().values(SOULSEEK_TASK_KEY_TEMPLATE.formatted(trackId));
    }

    public void delete(SlskSearchTaskDto taskDto) {
        redisTemplate.opsForHash().delete(SOULSEEK_TASK_KEY_TEMPLATE.formatted(taskDto.trackId()), taskDto.uuid().toString());
    }
}
