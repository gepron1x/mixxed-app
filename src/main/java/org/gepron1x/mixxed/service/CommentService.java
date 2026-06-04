package org.gepron1x.mixxed.service;


import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.entity.Comment;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {


    private final CommentRepository repository;

    @Transactional
    public Comment addComment(User user, Mix mix, String content) {
        Comment comment = Comment.builder()
                .mix(mix)
                .author(user)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(comment);
        return comment;
    }

    public boolean canDelete(@Nullable User currentUser, Mix mix, Comment comment) {
        if(currentUser == null) return false;
        if(currentUser.isAdmin()) return true;
        if(currentUser.getId().equals(mix.getAuthor().getId())) return true;
        return currentUser.getId().equals(comment.getAuthor().getId());
    }

    @Transactional
    public void removeComment(long commentId) {
        repository.deleteById(commentId);
    }


}
