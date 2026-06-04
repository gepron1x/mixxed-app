package org.gepron1x.mixxed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.gepron1x.mixxed.entity.Mix;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public final class MixDTO {

    private final Long id;
    private final String authorUsername;
    private final String slug;

    private final String title;
    private final String description;

    private final String genre;
    private final Integer durationSeconds;

    private final String audioUrl;
    private final String coverUrl;

    private LocalDateTime uploadedAt;

    private long totalPlays;

    public static MixDTO create(Mix mix) {
        return new MixDTO(
                mix.getId(),
                mix.getAuthor().getUsername(),
                mix.getSlug(),
                mix.getTitle(),
                mix.getDescription(),
                mix.getGenre(),
                mix.getDurationSeconds(),
                mix.getAudioUrl(),
                mix.getCoverUrl(),
                mix.getUploadedAt(),
                mix.getTotalPlays()
        );
    }
}
