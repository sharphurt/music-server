package ru.sharphurt.musicserver.soulseek.dto.rest;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sharphurt.musicserver.library.enitiy.TrackEntity;
import ru.sharphurt.musicserver.soulseek.entity.SlskSearchTaskEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlskSearchResponseDto {

    private List<SlskSearchTaskEntity> createdTasks;

    private TrackEntity trackData;
}
