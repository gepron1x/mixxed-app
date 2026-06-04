package org.gepron1x.mixxed.controller;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.genre.Genre;
import org.gepron1x.mixxed.repository.MixRepository;
import org.gepron1x.mixxed.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChartsController {

    private final MixRepository mixRepository;
    private final UserService userService;

    @GetMapping("/charts")
    public String charts(@RequestParam(value = "genre", required = false) String genre,
                         Authentication auth, Model model) {
        User currentUser = userService.getCurrentUser(auth);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("genres", Genre.genres());
        model.addAttribute("selectedGenre", genre);

        if (genre != null && !genre.isBlank()) {
            model.addAttribute("mixes", mixRepository.findByGenreOrderByTotalPlaysDesc(genre));
        } else {
            model.addAttribute("mixes", mixRepository.findAllByOrderByTotalPlaysDesc());
        }
        return "charts";
    }
}
