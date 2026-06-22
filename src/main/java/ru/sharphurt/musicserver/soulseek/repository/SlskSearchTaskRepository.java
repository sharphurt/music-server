package ru.sharphurt.musicserver.soulseek.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sharphurt.musicserver.soulseek.entity.SlskSearchTaskEntity;

@Repository
public interface SlskSearchTaskRepository extends JpaRepository<SlskSearchTaskEntity, Long> {

    List<SlskSearchTaskEntity> findByTrackId(Long trackId);

}
