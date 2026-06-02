package org.gepron1x.mixxed.controller;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.form.ProfileEditForm;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ProfileEditController {

    private final UserService userService;

    @GetMapping("/settings")
    public String settingsPage(Authentication auth, Model model) {
        User currentUser = userService.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";
        ProfileEditForm form = new ProfileEditForm();
        form.setBio(currentUser.getBio());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("form", form);
        return "profile-edit";
    }

    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute ProfileEditForm form, Authentication auth, Model model) {
        User currentUser = userService.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";
        try {
            userService.updateProfile(currentUser, form);
            return "redirect:/u/" + currentUser.getUsername();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("form", form);
            return "profile-edit";
        }
    }
}
