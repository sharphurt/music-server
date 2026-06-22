package ru.sharphurt.musicserver.soulseek.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "slsk_search_task")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlskSearchTaskEntity {

    @Id
    private UUID uuid;

    private Long trackId;

    private String query;

    private boolean disabled;
}
