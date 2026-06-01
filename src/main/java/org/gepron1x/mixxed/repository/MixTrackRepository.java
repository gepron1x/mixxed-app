package org.gepron1x.mixxed.repository;

import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.MixTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MixTrackRepository extends JpaRepository<MixTrack, Long> {
    List<MixTrack> findByMixOrderByPosition(Mix mix);
}
