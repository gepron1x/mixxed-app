package org.gepron1x.mixxed.service;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.entity.*;
import org.gepron1x.mixxed.form.MixUploadForm;
import org.gepron1x.mixxed.repository.CommentRepository;
import org.gepron1x.mixxed.repository.FollowRepository;
import org.gepron1x.mixxed.repository.LikeRepository;
import org.gepron1x.mixxed.repository.MixRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MixService {

    private final MixRepository mixRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final StorageService storageService;

    @Transactional
    public Mix uploadMix(User author, MixUploadForm form) {
        System.out.println("Received the form!");
        String slug = generateSlug(form.getTitle());

        String audioKey = null;
        String coverKey = null;

        if (form.getAudioFile() != null && !form.getAudioFile().isEmpty()) {
            audioKey = "audio/" + author.getId() + "/" + UUID.randomUUID();
            storageService.uploadAudio(form.getAudioFile(), audioKey);
        }
        if (form.getCoverFile() != null && !form.getCoverFile().isEmpty()) {
            coverKey = "covers/" + author.getId() + "/" + UUID.randomUUID();
            storageService.uploadImage(form.getCoverFile(), coverKey);
        }

        Mix mix = Mix.builder()
            .author(author)
            .slug(slug)
            .title(form.getTitle())
            .description(form.getDescription())
            .audioUrl(audioKey)
            .coverUrl(coverKey)
            .uploadedAt(LocalDateTime.now())
            .genre(form.getGenre())
            .totalPlays(0)
            .tracks(new ArrayList<>())
            .comments(new ArrayList<>())
            .playlists(new ArrayList<>())
            .likes(new ArrayList<>())
            .build();

        mix = mixRepository.save(mix);

        if (form.getTracks() != null) {
            List<MixTrack> tracks = new ArrayList<>();
            for (int i = 0; i < form.getTracks().size(); i++) {
                MixUploadForm.TrackEntry entry = form.getTracks().get(i);
                if (entry.getArtist() == null || entry.getArtist().isBlank()) continue;
                MixTrack track = MixTrack.builder()
                    .mix(mix)
                    .position(i + 1)
                    .startTimeSeconds(parseTime(entry.getStartTime()))
                    .artist(entry.getArtist())
                    .title(entry.getTitle() != null ? entry.getTitle() : "")
                    .build();
                tracks.add(track);
            }
            mix.setTracks(tracks);
            mix = mixRepository.save(mix);
        }

        return mix;
    }

    @Transactional
    public boolean toggleLike(User user, Mix mix) {
        var existing = likeRepository.findByUserAndMix(user, mix);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return false;
        } else {
            likeRepository.save(Like.builder().user(user).mix(mix).build());
            return true;
        }
    }

    @Transactional
    public void incrementPlays(Mix mix) {
        mix.setTotalPlays(mix.getTotalPlays() + 1);
        mixRepository.save(mix);
    }


    @Transactional
    public void addComment(User user, Mix mix, String content) {
        Comment comment = Comment.builder()
            .mix(mix)
            .author(user)
            .content(content)
            .createdAt(LocalDateTime.now())
            .build();
        commentRepository.save(comment);
    }

    private String generateSlug(String title) {
        String base = title.toLowerCase()
            .replaceAll("[^a-z0-9а-яё\\s]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
        if (base.isBlank()) base = "mix";
        String suffix = UUID.randomUUID().toString().substring(0, 4);
        String slug = base + "-" + suffix;
        // ensure uniqueness
        while (mixRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 4);
        }
        return slug;
    }

    private int parseTime(String time) {
        if (time == null || time.isBlank()) return 0;
        try {
            String[] parts = time.split(":");
            if (parts.length == 2) {
                return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            }
            return Integer.parseInt(time);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
