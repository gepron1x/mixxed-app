package org.gepron1x.mixxed.service;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.Playlist;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.PlaylistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    @Transactional
    public Playlist createPlaylist(User user, String title, String description) {
        String slug = generateSlug(title);
        Playlist playlist = Playlist.builder()
            .author(user)
            .slug(slug)
            .title(title)
            .description(description)
            .build();
        return playlistRepository.save(playlist);
    }

    @Transactional
    public void addMixToPlaylist(Playlist playlist, Mix mix) {
        if (!playlist.getMixes().contains(mix)) {
            playlist.getMixes().add(mix);
            playlistRepository.save(playlist);
        }
    }

    @Transactional
    public void removeMixFromPlaylist(Playlist playlist, Mix mix) {
        playlist.getMixes().remove(mix);
        playlistRepository.save(playlist);
    }

    private String generateSlug(String title) {
        String base = title.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
        if (base.isBlank()) base = "playlist";
        if (base.length() > 24) base = base.substring(0, 24);
        String slug = base + "-" + UUID.randomUUID().toString().substring(0, 4);
        while (playlistRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 4);
        }
        return slug;
    }
}
