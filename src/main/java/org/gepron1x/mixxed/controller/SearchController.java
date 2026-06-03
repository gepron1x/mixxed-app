package org.gepron1x.mixxed.controller;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.MixRepository;
import org.gepron1x.mixxed.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final MixRepository mixRepository;
    private final UserService userService;

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", defaultValue = "") String query,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "30") int size,
                         @RequestParam(defaultValue = "title") String sortBy,
                         @RequestParam(defaultValue = "asc") String direction,
                         Authentication auth, Model model) {
        User currentUser = userService.getCurrentUser(auth);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("query", query);

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Mix> results = mixRepository
                .findByTitleContainingIgnoreCaseOrAuthor_UsernameContainingIgnoreCase(
                        query, query, pageable);
        if (!query.isBlank()) {
            model.addAttribute("results", results);
        } else {
            model.addAttribute("results", java.util.List.of());
        }
        return "search";
    }
}
