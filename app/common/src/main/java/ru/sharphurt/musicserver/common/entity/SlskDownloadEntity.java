package ru.sharphurt.musicserver.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "slsk_download")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlskDownloadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid")
    private UUID uuid;

    @JsonIgnore
    @Column(name = "transfer_id")
    private UUID transferId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "track_id")
    private TrackEntity trackMetadata;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "intent", nullable = false)
    @Enumerated(EnumType.STRING)
    private DownloadIntent downloadIntent;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DownloadStatus downloadStatus;

    @Column(name = "local_filename")
    private String localFilename;

    @Column(name = "library_path")
    private String libraryFilename;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "slsk_username")
    private String slskUsername;

    @Column(name = "slsk_filename")
    private String slskFilename;

    @Column(name = "slsk_filesize")
    private long slskFilesize;
}
