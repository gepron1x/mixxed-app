package org.gepron1x.mixxed.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.entity.*;
import org.gepron1x.mixxed.form.MixUploadForm;
import org.gepron1x.mixxed.repository.CommentRepository;
import org.gepron1x.mixxed.repository.FollowRepository;
import org.gepron1x.mixxed.repository.LikeRepository;
import org.gepron1x.mixxed.repository.MixRepository;
import org.gepron1x.mixxed.util.TimeUtil;
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


    @Nullable
    private List<MixTrack> mapTracks(Mix mix, MixUploadForm form) {
        if (form.getTracks() == null) return null;
        List<MixTrack> tracks = new ArrayList<>();
        for (int i = 0; i < form.getTracks().size(); i++) {
            MixUploadForm.TrackEntry entry = form.getTracks().get(i);
            if (entry.getArtist() == null || entry.getArtist().isBlank()) continue;
            MixTrack track = MixTrack.builder()
                    .mix(mix)
                    .position(i + 1)
                    .startTimeSeconds(TimeUtil.parseTime(entry.getStartTime()))
                    .artist(entry.getArtist())
                    .title(entry.getTitle() != null ? entry.getTitle() : "")
                    .build();
            tracks.add(track);
        }
        return tracks;
    }

    @Transactional
    public Mix uploadMix(User author, MixUploadForm form) {
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

        mix.setTracks(mapTracks(mix, form));
        return mixRepository.save(mix);
    }

    @Transactional
    public Mix updateMix(Mix mix, MixUploadForm form) {
        if(form.getTitle() != null) mix.setTitle(form.getTitle());
        if(form.getDescription() != null) mix.setDescription(form.getDescription());

        if (form.getAudioFile() != null && !form.getAudioFile().isEmpty()) {
            String audioKey = mix.getAudioUrl() != null ? mix.getAudioUrl() :
                    "audio/" + mix.getAuthor().getId() + "/" + UUID.randomUUID();
            storageService.uploadAudio(form.getAudioFile(), audioKey);
        }
        if (form.getCoverFile() != null && !form.getCoverFile().isEmpty()) {
            String coverKey = mix.getCoverUrl() != null ? mix.getCoverUrl() :
                    "covers/" + mix.getAuthor().getId() + "/" + UUID.randomUUID();
            storageService.uploadImage(form.getCoverFile(), coverKey);
        }

        if(form.getGenre() != null) mix.setGenre(form.getGenre());


        List<MixTrack> newTracks = mapTracks(mix, form);
        mix.getTracks().clear();
        if (newTracks != null) {
            mix.getTracks().addAll(newTracks);
        }

        return mixRepository.save(mix);
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
    public void deleteMix(Mix mix) {
        storageService.deleteObject(mix.getAudioUrl());
        storageService.deleteObject(mix.getCoverUrl());
        mixRepository.delete(mix);
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

}
