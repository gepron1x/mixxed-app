package org.gepron1x.mixxed.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gepron1x.mixxed.entity.Like;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.FollowRepository;
import org.gepron1x.mixxed.repository.LikeRepository;
import org.gepron1x.mixxed.repository.MixRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Рекомендательный сервис.
 *
 * <p>Алгоритм для авторизованного пользователя вычисляет взвешенный score для
 * каждого кандидата по четырём сигналам:
 * <ol>
 *   <li><b>Жанровое сходство</b> — доля лайков пользователя в жанре кандидата.</li>
 *   <li><b>Буст за подписку</b> — сильный бонус, если автор в списке подписок.</li>
 *   <li><b>Авторское сходство</b> — доля лайков у миксов этого автора.</li>
 *   <li><b>Популярность + свежесть</b> — log(plays+1) с экспоненциальным decay.</li>
 * </ol>
 * После ранжирования применяется diversity-фильтр (не более {@value MAX_PER_AUTHOR}
 * миксов одного автора).
 *
 * <p>Для анонимных пользователей или новичков без истории используется
 * простой фоллбэк: популярность + свежесть.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    // Веса сигналов
    private static final double W_POPULARITY    = 1.0;
    private static final double W_GENRE         = 4.0;
    private static final double W_FOLLOW        = 6.0;
    private static final double W_AUTHOR_LIKE   = 3.0;
    private static final double W_FRESHNESS     = 2.0;

    // Параметры пула кандидатов
    private static final int CANDIDATE_POOL     = 100;
    private static final int MAX_PER_AUTHOR     = 3;
    private static final double FRESHNESS_HALF_LIFE_DAYS = 30.0;

    private final MixRepository    mixRepository;
    private final LikeRepository   likeRepository;
    private final FollowRepository followRepository;

    private record UserProfile(
            Set<Long>         likedMixIds,
            Map<String, Long> genreLikes,
            Map<Long, Long>   authorLikes,
            Set<Long>         followedAuthorIds,
            int               totalLikes
    ) {}

    private record ScoredMix(Mix mix, double score) {}

    @Transactional(readOnly = true)
    public List<Mix> recommend(@Nullable User user, int n) {
        List<Mix> candidates = mixRepository.findRecentCandidates(PageRequest.of(0, CANDIDATE_POOL));

        if (user == null) {
            return rankByPopularityAndFreshness(candidates, n);
        }

        UserProfile profile = buildUserProfile(user);

        List<Mix> filtered = candidates.stream()
                .filter(m -> !m.getAuthor().getId().equals(user.getId()))
                .filter(m -> !profile.likedMixIds().contains(m.getId()))
                .collect(Collectors.toList());

        if (profile.totalLikes() == 0 && profile.followedAuthorIds().isEmpty()) {
            log.debug("User {} has no history – falling back to popularity ranking", user.getUsername());
            return rankByPopularityAndFreshness(filtered, n);
        }

        return rankPersonalized(filtered, profile, n);
    }

    private UserProfile buildUserProfile(User user) {
        List<Like> likes = likeRepository.findByUser(user);
        Set<Long> likedMixIds = likes.stream()
                .map(l -> l.getMix().getId())
                .collect(Collectors.toSet());

        Map<String, Long> genreLikes = likes.stream()
                .filter(l -> l.getMix().getGenre() != null && !l.getMix().getGenre().isBlank())
                .collect(Collectors.groupingBy(l -> l.getMix().getGenre(), Collectors.counting()));

        Map<Long, Long> authorLikes = likes.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getMix().getAuthor().getId(),
                        Collectors.counting()));

        Set<Long> followedAuthorIds = followRepository.findByFollower(user).stream()
                .map(f -> f.getFollowed().getId())
                .collect(Collectors.toSet());

        return new UserProfile(
                likedMixIds,
                genreLikes,
                authorLikes,
                followedAuthorIds,
                likes.size()
        );
    }

    private List<Mix> rankPersonalized(List<Mix> candidates, UserProfile profile, int n) {
        List<ScoredMix> scored = candidates.stream()
                .map(mix -> new ScoredMix(mix, computeScore(mix, profile)))
                .sorted(Comparator.comparingDouble(ScoredMix::score).reversed())
                .collect(Collectors.toList());

        log.debug("Scored {} candidates for personalized feed, top score = {}",
                scored.size(), scored.isEmpty() ? 0 : scored.get(0).score());

        return applyDiversityFilter(scored, n);
    }

    private double computeScore(Mix mix, UserProfile profile) {
        double score = 0;

        // 1. Популярность (логарифмическая шкала)
        score += W_POPULARITY * popularityScore(mix);

        // 2. Жанровое сходство
        if (mix.getGenre() != null && !mix.getGenre().isBlank() && profile.totalLikes() > 0) {
            long genreCount = profile.genreLikes().getOrDefault(mix.getGenre(), 0L);
            double genreAffinity = (double) genreCount / profile.totalLikes();
            score += W_GENRE * genreAffinity;
        }

        // 3. Буст за подписку
        if (profile.followedAuthorIds().contains(mix.getAuthor().getId())) {
            score += W_FOLLOW;
            // Дополнительный бонус: популярные миксы подписанных авторов ценнее
            score += 0.3 * popularityScore(mix);
        }

        // 4. Авторское сходство через лайки
        if (profile.totalLikes() > 0) {
            long authorLikesCount = profile.authorLikes().getOrDefault(mix.getAuthor().getId(), 0L);
            if (authorLikesCount > 0) {
                double authorAffinity = (double) authorLikesCount / profile.totalLikes();
                score += W_AUTHOR_LIKE * authorAffinity;
            }
        }

        // 5. Свежесть
        score += W_FRESHNESS * freshnessScore(mix);

        return score;
    }

    private List<Mix> rankByPopularityAndFreshness(List<Mix> candidates, int n) {
        return candidates.stream()
                .sorted(Comparator.comparingDouble(
                        (Mix m) -> -(popularityScore(m) * W_POPULARITY + freshnessScore(m) * W_FRESHNESS)))
                .limit(n)
                .collect(Collectors.toList());
    }

    private List<Mix> applyDiversityFilter(List<ScoredMix> scored, int n) {
        Map<Long, Integer> authorCount = new HashMap<>();
        List<Mix> result = new ArrayList<>(n);
        List<Mix> overflow = new ArrayList<>();

        for (ScoredMix sm : scored) {
            if (result.size() >= n) break;
            long authorId = sm.mix().getAuthor().getId();
            int count = authorCount.getOrDefault(authorId, 0);
            if (count < MAX_PER_AUTHOR) {
                result.add(sm.mix());
                authorCount.put(authorId, count + 1);
            } else {
                overflow.add(sm.mix());
            }
        }

        if (result.size() < n) {
            int remaining = n - result.size();
            overflow.stream().limit(remaining).forEach(result::add);
        }

        return result;
    }


    private double popularityScore(Mix mix) {
        return Math.log1p(mix.getTotalPlays());
    }

    private double freshnessScore(Mix mix) {
        if (mix.getUploadedAt() == null) return 0.0;
        long daysOld = ChronoUnit.DAYS.between(mix.getUploadedAt(), LocalDateTime.now());
        return Math.pow(2.0, -(double) daysOld / FRESHNESS_HALF_LIFE_DAYS);
    }


}
