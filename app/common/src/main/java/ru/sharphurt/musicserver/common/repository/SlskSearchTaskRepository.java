package ru.sharphurt.musicserver.common.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sharphurt.musicserver.common.entity.SlskSearchTaskEntity;

@Repository
public interface SlskSearchTaskRepository extends JpaRepository<SlskSearchTaskEntity, Long> {

    List<SlskSearchTaskEntity> findAllByTrackIdAndDisabledFalse(Long trackId);

}
