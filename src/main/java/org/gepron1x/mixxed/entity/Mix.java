package org.gepron1x.mixxed.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "mixes")
public class Mix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(nullable = false)
    private String title;

    private String description;

    private String genre;

    private Integer durationSeconds;

    private String audioUrl;

    private String coverUrl;

    private LocalDateTime uploadedAt;

    @OneToMany(mappedBy = "mix", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MixTrack> tracks = new ArrayList<>();

    @OneToMany(mappedBy = "mix", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "mixes")
    private List<Playlist> playlists = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "mix", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Like> likes = new ArrayList<>();

    private long totalPlays;

}
