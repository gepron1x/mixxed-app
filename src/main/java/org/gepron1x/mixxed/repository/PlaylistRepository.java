package org.gepron1x.mixxed.repository;

import org.gepron1x.mixxed.entity.Playlist;
import org.gepron1x.mixxed.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    Optional<Playlist> findBySlug(String slug);

    List<Playlist> findByAuthor(User author);
}
