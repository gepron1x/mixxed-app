package org.gepron1x.mixxed.repository;


import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MixRepository extends JpaRepository<Mix, Long> {

    Optional<Mix> findBySlug(String slug);

    Collection<Mix> findByAuthor(User author);

    List<Mix> findTop10ByOrderByTotalPlaysDesc();

    Page<Mix> findByTitleContainingIgnoreCaseOrAuthor_UsernameContainingIgnoreCase(
            String title, String username, Pageable pageable);
}
