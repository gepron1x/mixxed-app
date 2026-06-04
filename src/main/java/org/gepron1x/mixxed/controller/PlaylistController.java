package org.gepron1x.mixxed.controller;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.dto.PlaylistDTO;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.Playlist;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.MixRepository;
import org.gepron1x.mixxed.repository.PlaylistRepository;
import org.gepron1x.mixxed.service.PlaylistService;
import org.gepron1x.mixxed.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistRepository playlistRepository;
    private final MixRepository mixRepository;
    private final PlaylistService playlistService;
    private final UserService userService;

    @GetMapping("/playlist/{slug}")
    public String playlistPage(@PathVariable String slug, Authentication auth, Model model) {
        Playlist playlist = playlistRepository.findBySlug(slug).orElse(null);
        if (playlist == null) return "redirect:/";
        User currentUser = userService.getCurrentUser(auth);
        model.addAttribute("playlist", PlaylistDTO.create(playlist));
        model.addAttribute("currentUser", currentUser);
        return "playlist";
    }

    @PostMapping("/playlist/create")
    public String createPlaylist(@RequestParam String title,
                                 @RequestParam(required = false) String description,
                                 Authentication auth) {
        User currentUser = userService.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";
        Playlist playlist = playlistService.createPlaylist(currentUser, title, description);
        return "redirect:/playlist/" + playlist.getSlug();
    }

    @PostMapping("/playlist/{slug}/delete")
    public String deletePlaylist(@PathVariable String slug,
                                 Authentication auth) {
        User currentUser = userService.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";
        Playlist playlist = playlistRepository.findBySlug(slug).orElse(null);
        if (playlist == null) return "redirect:/";
        playlistRepository.delete(playlist);
        return "redirect:/u/" + playlist.getAuthor().getUsername();
    }

    @PostMapping("/playlist/{slug}/add")
    public String addToPlaylist(@PathVariable String slug,
                                @RequestParam Long mixId,
                                Authentication auth) {
        User currentUser = userService.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";
        Playlist playlist = playlistRepository.findBySlug(slug).orElse(null);
        if (playlist == null) return "redirect:/";
        Mix mix = mixRepository.findById(mixId).orElse(null);
        if (mix == null) return "redirect:/";
        if (playlist.getAuthor().getId().equals(currentUser.getId())) {
            playlistService.addMixToPlaylist(playlist, mix);
        }
        return "redirect:/mix/" + mix.getSlug();
    }
}
