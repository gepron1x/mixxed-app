package org.gepron1x.mixxed.controller;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.dto.CommentDTO;
import org.gepron1x.mixxed.entity.Comment;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.CommentRepository;
import org.gepron1x.mixxed.repository.MixRepository;
import org.gepron1x.mixxed.service.CommentService;
import org.gepron1x.mixxed.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class CommentController {

    private final UserService userService;
    private final CommentService commentService;

    private final MixRepository mixRepository;
    private final CommentRepository commentRepository;


    @PostMapping("/mix/{slug}/comment")
    public ResponseEntity<CommentDTO> addComment(@PathVariable String slug,
                                                 @RequestParam String content,
                                                 Authentication auth) {
        User currentUser = userService.getCurrentUser(auth);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Mix mix = mixRepository.findBySlug(slug).orElse(null);
        if (mix == null) return ResponseEntity.notFound().build();
        CommentDTO comment = CommentDTO.create(
                this.commentService.addComment(currentUser, mix, content), true
        );
        return ResponseEntity.ok(comment);
    }


    @DeleteMapping("/comment/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable long id,
            Authentication auth) {
        User currentUser = userService.getCurrentUser(auth);
        Comment comment = commentRepository.findById(id).orElse(null);
        if(comment == null) return ResponseEntity.notFound().build();

        if(!commentService.canDelete(currentUser, comment.getMix(), comment)
        ) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        commentService.removeComment(id);
        return ResponseEntity.ok().build();
    }
}
