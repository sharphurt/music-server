package ru.sharphurt.musicserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.sharphurt.musicserver.dto.TrackDto;
import ru.sharphurt.musicserver.soulseek.dto.SoulseekSearchTaskDto;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, TrackDto> trackRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, TrackDto> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(TrackDto.class));
        return template;
    }

    @Bean
    public RedisTemplate<String, SoulseekSearchTaskDto> soulseekTaskRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, SoulseekSearchTaskDto> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        var stringSerializer = new StringRedisSerializer();
        var jsonSerializer = new JacksonJsonRedisSerializer<>(SoulseekSearchTaskDto.class);

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);

        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        return template;
    }
}
