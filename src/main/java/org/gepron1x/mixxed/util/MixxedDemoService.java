package org.gepron1x.mixxed.util;


import lombok.AllArgsConstructor;
import org.gepron1x.mixxed.entity.Like;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.form.RegisterForm;
import org.gepron1x.mixxed.genre.Genre;
import org.gepron1x.mixxed.repository.LikeRepository;
import org.gepron1x.mixxed.repository.MixRepository;
import org.gepron1x.mixxed.service.MixService;
import org.gepron1x.mixxed.service.StorageService;
import org.gepron1x.mixxed.service.UserService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@AllArgsConstructor
public final class MixxedDemoService {

    private final UserService userService;
    private final MixRepository mixRepository;
    private final LikeRepository likeRepository;

    private final JdbcTemplate jdbcTemplate;

    private final S3Client s3Client;

    private void putDemoObject(String key, String contentType, String fileName) {
        try {
            s3Client.createBucket(b -> b.bucket("mixxed"));
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException ex) {
            //
        }
        Resource resource = new ClassPathResource("demo/" + fileName);
        try (InputStream inputStream = resource.getInputStream()) {
            long contentLength = resource.contentLength();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket("mixxed")
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(inputStream, contentLength));
        } catch (Exception ignored) {
            throw new RuntimeException(ignored);
        }
    }

    public void buildDemonstrationEntities() {
        putDemoObject("audio/test", "audio/mpeg", "audio.mp3");
        putDemoObject("cover/test", "image/jpeg", "image.jpg");
        List<User> users = buildUsers(10);
        List<Mix> mixes = new ArrayList<>();
        for(User user : users) {
            mixes.addAll(buildMixes(4, user, "audio/test", "cover/test"));
        }
        buildLikes(mixes, users);
    }

    public void dropAll() {
        jdbcTemplate.execute("DELETE FROM users WHERE username LIKE 'mixxed_testuser%'");
    }

    private List<User> buildUsers(int amount) {
        List<User> users = new ArrayList<>();
        for(int i = 0; i < amount; i++) {
            RegisterForm form = new RegisterForm();
            form.setUsername("mixxed_testuser" + i);
            form.setEmail("testuser" + i + "@mail.ru");
            form.setPassword("password");
            form.setConfirmPassword("password");

            User user = userService.register(form);
            users.add(user);
        }
        return users;
    }

    private List<Mix> buildMixes(int amount, User author, String audioKey, String coverKey) {
        List<Mix> mixes = new ArrayList<>();
        for(int i = 0; i < amount; i++) {
            Mix mix = Mix.builder().author(author)
                    .slug(author.getUsername() + "-mix" + i)
                    .title("Test Mix " + i)
                    .description("description")
                    .audioUrl(audioKey)
                    .coverUrl(coverKey)
                    .uploadedAt(LocalDateTime.now())
                    .genre(Genre.values()[ThreadLocalRandom.current().nextInt(Genre.values().length)].getFullName())
                    .totalPlays(ThreadLocalRandom.current().nextInt(10000))
                    .tracks(new ArrayList<>())
                    .comments(new ArrayList<>())
                    .playlists(new ArrayList<>())
                    .likes(new ArrayList<>())
                    .build();
            mixes.add(mix);
        }
        mixes = mixRepository.saveAll(mixes);
        return mixes;
    }

    public List<Like> buildLikes(List<Mix> mixes, List<User> users) {
        List<Like> likes = new ArrayList<>();
        for(Mix mix : mixes) {
            int likesCount = ThreadLocalRandom.current().nextInt(users.size());
            for(int i = 0; i < likesCount; i++) {
                User user = users.get(i);
                likes.add(Like.builder().mix(mix).user(user).build());
            }
        }
        likes = likeRepository.saveAll(likes);
        return likes;
    }
}
