package org.gepron1x.mixxed.controller;


import lombok.AllArgsConstructor;
import org.gepron1x.mixxed.dto.MixDTO;
import org.gepron1x.mixxed.dto.PlaylistDTO;
import org.gepron1x.mixxed.entity.Playlist;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.service.RecommendationService;
import org.gepron1x.mixxed.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@AllArgsConstructor
public class RecommendationsController {

    private final UserService userService;
    private final RecommendationService recommendationService;

    @GetMapping("/recommendations")
    public String recommendations(Authentication auth, Model model) {
        User currentUser = userService.getCurrentUser(auth);
        model.addAttribute("playlist", PlaylistDTO.builder()
                .id(-1L)
                .title("Рекомендации")
                .description("Плейлист, собранный только для вас")
                .mixes(recommendationService.recommend(currentUser, 10).stream()
                        .map(MixDTO::create).toList()).build()
        );
        model.addAttribute("currentUser", currentUser);
        return "playlist";
    }
}
