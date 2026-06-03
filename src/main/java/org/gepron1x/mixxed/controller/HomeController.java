package org.gepron1x.mixxed.controller;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.MixRepository;
import org.gepron1x.mixxed.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final MixRepository mixRepository;

    @GetMapping("/")
    public String home(Authentication auth, Model model) {
        User currentUser = userService.getCurrentUser(auth);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("popular", mixRepository.findTop10ByOrderByTotalPlaysDesc());
        return "index";
    }
}