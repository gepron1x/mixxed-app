package org.gepron1x.mixxed.repository;


import org.gepron1x.mixxed.entity.Mix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MixRepository extends JpaRepository<Mix, Long> {

    Optional<Mix> findBySlug(String slug);
}
