package org.gepron1x.mixxed.service;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.form.RegisterForm;
import org.gepron1x.mixxed.entity.Follow;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.FollowRepository;
import org.gepron1x.mixxed.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userRepository.findByUsername(form.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.findByEmail(form.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
            .username(form.getUsername())
            .email(form.getEmail())
            .passwordHash(passwordEncoder.encode(form.getPassword()))
            .registrationDate(LocalDate.now())
            .build();
        return userRepository.save(user);
    }

    public User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }


    @Transactional
    public boolean toggleFollow(User follower, User followed) {
        var existing = followRepository.findByFollowerAndFollowed(follower, followed);
        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            return false;
        } else {
            followRepository.save(Follow.builder().follower(follower).followed(followed).build());
            return true;
        }
    }
}
