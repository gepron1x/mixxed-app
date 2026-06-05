package org.gepron1x.mixxed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.Playlist;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class PlaylistDTO {

    private final Long id;
    private final String authorUsername;
    private final String slug;

    private final String title;
    private final String description;

    private final List<MixDTO> mixes;

    public static PlaylistDTO create(Playlist playlist) {
        return new PlaylistDTO(
                playlist.getId(),
                playlist.getAuthor().getUsername(),
                playlist.getSlug(),
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getMixes().stream().map(MixDTO::create).toList()
        );
    }

}
