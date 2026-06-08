package org.gepron1x.mixxed.controller;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.form.MixUploadForm;
import org.gepron1x.mixxed.genre.Genre;
import org.gepron1x.mixxed.repository.MixRepository;
import org.gepron1x.mixxed.service.MixService;
import org.gepron1x.mixxed.service.UserService;
import org.gepron1x.mixxed.util.TimeUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class UploadController {

    private final MixService mixService;
    private final UserService userService;

    @GetMapping("/upload")
    public String uploadPage(Authentication auth, Model model) {
        User currentUser = userService.getCurrentUser(auth);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("form", new MixUploadForm());
        model.addAttribute("genres", Genre.genres());
        model.addAttribute("isEdit", false);
        return "upload";
    }

    @PostMapping("/upload")
    public String upload(@ModelAttribute MixUploadForm form, Authentication auth, Model model) {
        User currentUser = userService.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";
        try {
            Mix mix = mixService.uploadMix(currentUser, form);
            return "redirect:/mix/" + URLEncoder.encode(mix.getSlug(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("genres", Genre.genres());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isEdit", false);
            return "upload";
        }
    }
}
