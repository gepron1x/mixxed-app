package org.gepron1x.mixxed.repository;

import org.gepron1x.mixxed.entity.Comment;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.mix.author = :author")
    int countAllCommentsByAuthor(@Param("author") User author);

    List<Comment> findByMixOrderByCreatedAtDesc(Mix mix);

    int countByMix(Mix mix);
}
