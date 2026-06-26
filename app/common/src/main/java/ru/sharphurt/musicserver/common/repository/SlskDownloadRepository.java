package ru.sharphurt.musicserver.common.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sharphurt.musicserver.common.entity.DownloadStatus;
import ru.sharphurt.musicserver.common.entity.SlskDownloadEntity;
import ru.sharphurt.musicserver.common.entity.UserEntity;

@Repository
public interface SlskDownloadRepository extends JpaRepository<SlskDownloadEntity, UUID> {

    List<SlskDownloadEntity> findAllByUser(UserEntity user);

    SlskDownloadEntity findByTransferId(UUID transferId);

    SlskDownloadEntity findByUserAndUuid(UserEntity user, UUID uuid);

    SlskDownloadEntity findByUserAndSlskUsernameAndSlskFilenameAndDownloadStatusNotIn(
        UserEntity user,
        String slskUsername,
        String slskFilename,
        List<DownloadStatus> excludedStatuses
    );

    @Query("""
        select download from SlskDownloadEntity download
        where download.downloadStatus = ru.sharphurt.musicserver.common.entity.DownloadStatus.IN_LIBRARY
        and download.localFilename is not null
        """)
    List<SlskDownloadEntity> findAllAddedToLibrary();

    @Query("""
        select download from SlskDownloadEntity download
        where download.downloadStatus in (ru.sharphurt.musicserver.common.entity.DownloadStatus.FAILED, ru.sharphurt.musicserver.common.entity.DownloadStatus.QUEUED)
        and download.localFilename is not null
        and download.requestedAt < :thresholdDate
        """)
    List<SlskDownloadEntity> findAllFailedLegacy(LocalDateTime thresholdDate);
}
