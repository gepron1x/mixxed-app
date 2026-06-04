package org.gepron1x.mixxed.service;


import lombok.AllArgsConstructor;
import org.gepron1x.mixxed.dto.UserStatistics;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.CommentRepository;
import org.gepron1x.mixxed.repository.LikeRepository;
import org.gepron1x.mixxed.repository.MixRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@AllArgsConstructor
public final class StatisticsService {

    private final MixRepository mixRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public UserStatistics gatherStatistics(User author) {
        Collection<Mix> mixes = mixRepository.findByAuthor(author);
        int totalMixes = mixes.size();

        if (totalMixes == 0) {
            return new UserStatistics(
                    0, 0, 0, 0, null, 0, 0);
        }

        int totalPlays = mixRepository.sumTotalPlaysByAuthor(author);

        int totalLikes = likeRepository.countAllLikesByAuthor(author);
        int totalComments = commentRepository.countAllCommentsByAuthor(author);

        Mix mostPopularMix = mixRepository.findMostPopularMixByAuthorId(author.getId()).orElse(null);

        int mostPopularMixLikes = 0;
        int mostPopularMixComments = 0;

        if (mostPopularMix != null) {
            mostPopularMixLikes = likeRepository.countByMix(mostPopularMix);
            mostPopularMixComments = commentRepository.countByMix(mostPopularMix);
        }

        return UserStatistics.builder()
                .totalMixes(totalMixes)
                .totalPlays(totalPlays)
                .totalLikes(totalLikes)
                .totalComments(totalComments)
                .mostPopularMix(mostPopularMix)
                .mostPopularMixLikes(mostPopularMixLikes)
                .mostPopularMixComments(mostPopularMixComments)
                .build();
    }
}
