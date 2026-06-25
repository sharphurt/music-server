package ru.sharphurt.musicserver.api.soulseek.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sharphurt.musicserver.common.entity.TrackEntity;
import ru.sharphurt.musicserver.common.entity.SlskSearchTaskEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlskSearchResponseDto {

    private List<SlskSearchTaskEntity> createdTasks;

    private TrackEntity trackData;
}
