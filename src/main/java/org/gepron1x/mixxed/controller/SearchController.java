package org.gepron1x.mixxed.controller;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.Playlist;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.MixRepository;
import org.gepron1x.mixxed.repository.PlaylistRepository;
import org.gepron1x.mixxed.repository.UserRepository;
import org.gepron1x.mixxed.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private static final int USER_LIMIT     = 20;
    private static final int PLAYLIST_LIMIT = 20;

    private final MixRepository      mixRepository;
    private final UserRepository     userRepository;
    private final PlaylistRepository playlistRepository;
    private final UserService        userService;

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public String search(
            @RequestParam(value = "q", defaultValue = "") String query,
            @RequestParam(defaultValue = "0")     int    page,
            @RequestParam(defaultValue = "30")    int    size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc")   String direction,
            Authentication auth, Model model) {

        User currentUser = userService.getCurrentUser(auth);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("query", query);

        if (query.isBlank()) {
            model.addAttribute("mixResults",      Page.empty());
            model.addAttribute("userResults",     List.of());
            model.addAttribute("playlistResults", List.of());
            return "search";
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Mix> mixResults = mixRepository
                .findByTitleContainingIgnoreCaseOrAuthor_UsernameContainingIgnoreCase(
                        query, query, pageable);

        Page<User> userResults = userRepository.findByUsernameContainingIgnoreCase(
                query, PageRequest.of(0, USER_LIMIT));

        Page<Playlist> playlistResults = playlistRepository.findByTitleContainingIgnoreCaseOrAuthor_UsernameContainingIgnoreCase(
                query, query, PageRequest.of(0, PLAYLIST_LIMIT));

        model.addAttribute("mixResults",      mixResults);
        model.addAttribute("userResults",     userResults);
        model.addAttribute("playlistResults", playlistResults);

        model.addAttribute("totalMixes",     mixResults.getTotalElements());
        model.addAttribute("totalUsers",     userResults.getTotalElements());
        model.addAttribute("totalPlaylists", playlistResults.getTotalElements());

        return "search";
    }
}
