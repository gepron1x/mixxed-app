package org.gepron1x.mixxed.controller;

import lombok.AllArgsConstructor;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.form.MixUploadForm;
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


@Controller
@AllArgsConstructor
public class MixEditController {

    private final UserService userService;
    private final MixRepository mixRepository;
    private final MixService mixService;

    @GetMapping("/mix/edit/{slug}")
    public String editPage(@PathVariable String slug, Authentication auth, Model model) {
        User currentUser = userService.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";

        Mix mix = mixRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!mix.getAuthor().getId().equals(currentUser.getId())) {
            return "redirect:/";
        }

        MixUploadForm form = new MixUploadForm();
        form.setTitle(mix.getTitle());
        form.setGenre(mix.getGenre());
        form.setDescription(mix.getDescription());
        form.setTracks(mix.getTracks().stream()
                .map(track -> new MixUploadForm.TrackEntry(
                        TimeUtil.formatSeconds(track.getStartTimeSeconds()),
                        track.getArtist(),
                        track.getTitle()
                )).toList());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("form", form);
        model.addAttribute("mixSlug", mix.getSlug());
        model.addAttribute("existingTracks", form.getTracks());
        model.addAttribute("coverUrl", mix.getCoverUrl() == null ? null : "/s3/" + mix.getCoverUrl());
        model.addAttribute("isEdit", true);

        return "upload";
    }

    @PostMapping("/mix/edit/{slug}")
    public String editMix(@PathVariable String slug, @ModelAttribute MixUploadForm form, Authentication auth, Model model) {
        User currentUser = userService.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";
        Mix mix = mixRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        mixService.updateMix(mix, form);
        try {
            return "redirect:/mix/" + slug;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isEdit", true);
            model.addAttribute("mixSlug", slug);
            return "upload";
        }
    }
}
