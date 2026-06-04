package org.gepron1x.mixxed.repository;

import org.gepron1x.mixxed.entity.Like;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndMix(User user, Mix mix);
    boolean existsByUserAndMix(User user, Mix mix);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.mix.author = :author")
    int countAllLikesByAuthor(@Param("author") User author);

    int countByMix(Mix mix);
    List<Like> findByUser(User user);
}
