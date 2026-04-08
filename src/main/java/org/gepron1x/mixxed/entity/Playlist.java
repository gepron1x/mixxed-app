package org.gepron1x.mixxed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="author_id")
    private User author;

    @Column(unique = true, nullable = false, length = 32)
    private String slug;

    @Column(nullable = false, length = 100)
    private String title;

    private String description;

    @ManyToMany
    @JoinTable(
            name = "playlist_mix",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "mix_id")
    )
    private List<Mix> mixes = new ArrayList<>();
}
