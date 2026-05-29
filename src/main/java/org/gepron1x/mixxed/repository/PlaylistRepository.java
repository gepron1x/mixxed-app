package org.gepron1x.mixxed.repository;

import org.gepron1x.mixxed.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    Optional<Playlist> findBySlug(String slug);
}
