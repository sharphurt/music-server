package ru.sharphurt.musicserver.soulseek.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sharphurt.musicserver.soulseek.entity.SlskDownloadEntity;
import ru.sharphurt.musicserver.user.entity.UserEntity;

public interface SlskDownloadRepository extends JpaRepository<SlskDownloadEntity, UUID> {

    List<SlskDownloadEntity> findAllByUser(UserEntity user);

    SlskDownloadEntity findByUserAndUuid(UserEntity user, UUID uuid);
}
