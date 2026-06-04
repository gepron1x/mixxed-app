package org.gepron1x.mixxed.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "mix_tracks")
public class MixTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mix_id", nullable = false)
    private Mix mix;

    private int position;

    private int startTimeSeconds;

    @Column(nullable = false)
    private String artist;

    @Column(nullable = false)
    private String title;
}
