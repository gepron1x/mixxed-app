package org.gepron1x.mixxed.controller;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.entity.Like;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.*;
import org.gepron1x.mixxed.service.UserService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;
    private final MixRepository mixRepository;
    private final LikeRepository likeRepository;
    private final PlaylistRepository playlistRepository;
    private final FollowRepository followRepository;
    private final UserService userService;

    @GetMapping("/u/{username}")
    public String profile(@PathVariable String username, Authentication auth, Model model) {
        User profileUser = userRepository.findByUsername(username).orElse(null);
        if (profileUser == null) return "redirect:/";

        User currentUser = userService.getCurrentUser(auth);
        boolean isOwn = currentUser != null && currentUser.getId().equals(profileUser.getId());
        boolean isFollowing = currentUser != null && !isOwn
            && followRepository.existsByFollowerAndFollowed(currentUser, profileUser);

        var mixes = mixRepository.findByAuthor(profileUser);
        var likes = likeRepository.findByUser(profileUser).stream().map(Like::getMix)
                .collect(Collectors.toList());
        var playlists = playlistRepository.findByAuthor(profileUser);
        long followersCount = followRepository.countByFollowed(profileUser);
        long followingCount = followRepository.countByFollower(profileUser);

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isOwn", isOwn);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("mixes", mixes);
        model.addAttribute("likedMixes", likes);
        model.addAttribute("playlists", playlists);
        model.addAttribute("followersCount", followersCount);
        model.addAttribute("followingCount", followingCount);
        return "user";
    }

    @PostMapping("/u/{username}/follow")
    public String toggleFollow(@PathVariable String username, Authentication auth) {
        User currentUser = userService.getCurrentUser(auth);
        if (currentUser == null) return "redirect:/login";
        User target = userRepository.findByUsername(username).orElse(null);
        if (target == null) return "redirect:/";
        userService.toggleFollow(currentUser, target);
        return "redirect:/u/" + username;
    }
}
