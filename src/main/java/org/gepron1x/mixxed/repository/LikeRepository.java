package org.gepron1x.mixxed.repository;

import org.gepron1x.mixxed.entity.Like;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndMix(User user, Mix mix);
    boolean existsByUserAndMix(User user, Mix mix);
    long countByMix(Mix mix);
    List<Like> findByUser(User user);
}
