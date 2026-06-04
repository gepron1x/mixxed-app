package org.gepron1x.mixxed.dto;


import jakarta.annotation.Nullable;
import lombok.Data;
import org.gepron1x.mixxed.entity.Comment;
import org.gepron1x.mixxed.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CommentDTO {

    private final long id;
    private final String authorName;
    private final String profilePictureUrl;

    private final String content;

    private final LocalDateTime createdAt;

    private final Integer trackTimeSeconds;

    private final boolean canDelete;


    public static CommentDTO create(Comment comment, boolean canDelete) {
        return new CommentDTO(
                comment.getId(),
                comment.getAuthor().getUsername(),
                comment.getAuthor().getProfilePictureUrl(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getTrackTimeSeconds(),
                canDelete
        );
    }
}
