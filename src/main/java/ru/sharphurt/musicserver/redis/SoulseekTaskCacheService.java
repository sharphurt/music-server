package ru.sharphurt.musicserver.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekSearchTaskDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SoulseekTaskCacheService {

    private static final String SOULSEEK_TASK_KEY_TEMPLATE = "soulseek:search:track:%s";

    private final RedisTemplate<String, SoulseekSearchTaskDto> redisTemplate;

    public void save(SoulseekSearchTaskDto taskDto) {
        redisTemplate.opsForHash().put(
                SOULSEEK_TASK_KEY_TEMPLATE.formatted(taskDto.trackId()),
                taskDto.uuid().toString(),
                taskDto);
    }

    public List<SoulseekSearchTaskDto> getAllForTrack(long trackId) {
        return redisTemplate.<String, SoulseekSearchTaskDto>opsForHash().values(SOULSEEK_TASK_KEY_TEMPLATE.formatted(trackId));
    }

    public void delete(SoulseekSearchTaskDto taskDto) {
        redisTemplate.opsForHash().delete(SOULSEEK_TASK_KEY_TEMPLATE.formatted(taskDto.trackId()), taskDto.uuid().toString());
    }
}
