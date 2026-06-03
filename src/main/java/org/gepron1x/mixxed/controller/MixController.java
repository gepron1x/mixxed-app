package org.gepron1x.mixxed.controller;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.entity.Comment;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.CommentRepository;
import org.gepron1x.mixxed.repository.LikeRepository;
import org.gepron1x.mixxed.repository.MixRepository;
import org.gepron1x.mixxed.repository.PlaylistRepository;
import org.gepron1x.mixxed.service.MixService;
import org.gepron1x.mixxed.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MixController {

    private final MixRepository mixRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final PlaylistRepository playlistRepository;
    private final MixService mixService;
    private final UserService userService;

    @GetMapping("/mix/{slug}")
    public String mixPage(@PathVariable String slug, Authentication auth, Model model) {
        Mix mix = mixRepository.findBySlug(slug).orElse(null);
        if (mix == null) return "redirect:/";

        User currentUser = userService.getCurrentUser(auth);
        boolean liked = currentUser != null && likeRepository.existsByUserAndMix(currentUser, mix);
        long likeCount = likeRepository.countByMix(mix);
        var comments = commentRepository.findByMixOrderByCreatedAtDesc(mix);

        model.addAttribute("mix", mix);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("liked", liked);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("comments", comments);

        if (currentUser != null) {
            model.addAttribute("userPlaylists", playlistRepository.findByAuthor(currentUser));
        }
        return "mix";
    }

    @PostMapping("/mix/{slug}/delete")
    public String delete(@PathVariable String slug, Authentication auth) {
        Mix mix = mixRepository.findBySlug(slug).orElse(null);
        if (mix == null) return "redirect:/";
        User currentUser = userService.getCurrentUser(auth);
        if(!mix.getAuthor().equals(currentUser) && !currentUser.isAdmin()) {
            throw new AccessDeniedException("You have no permission to delete this mix.");
        }
        mixService.deleteMix(mix);
        return "redirect:/";
    }

    @PostMapping("/api/mixes/{slug}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable String slug, Authentication auth) {
        User currentUser = userService.getCurrentUser(auth);
        if (currentUser == null) return ResponseEntity.status(401).build();
        Mix mix = mixRepository.findBySlug(slug).orElse(null);
        if (mix == null) return ResponseEntity.notFound().build();

        boolean liked = mixService.toggleLike(currentUser, mix);
        long count = likeRepository.countByMix(mix);
        return ResponseEntity.ok(Map.of("liked", liked, "count", count));
    }

    @PostMapping("/api/mixes/{slug}/play")
    @ResponseBody
    public ResponseEntity<Void> incrementPlay(@PathVariable String slug) {
        Mix mix = mixRepository.findBySlug(slug).orElse(null);
        if (mix == null) return ResponseEntity.notFound().build();
        mixService.incrementPlays(mix);
        return ResponseEntity.ok().build();
    }
}
