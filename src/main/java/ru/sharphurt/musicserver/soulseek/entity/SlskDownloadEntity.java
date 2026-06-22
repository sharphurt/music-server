package ru.sharphurt.musicserver.soulseek.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sharphurt.musicserver.locallibrary.enitiy.TrackEntity;
import ru.sharphurt.musicserver.user.entity.UserEntity;

@Entity
@Table(name = "slsk_download")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlskDownloadEntity {

    @Id
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
}
