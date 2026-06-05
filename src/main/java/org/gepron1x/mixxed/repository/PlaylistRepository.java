package org.gepron1x.mixxed.repository;

import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.Playlist;
import org.gepron1x.mixxed.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    Optional<Playlist> findBySlug(String slug);

    List<Playlist> findByAuthor(User author);

    List<Playlist> findTop10ByOrderByUploadedAtDesc();

    Page<Playlist> findByTitleContainingIgnoreCaseOrAuthor_UsernameContainingIgnoreCase(
            String title, String username, Pageable pageable);
}
