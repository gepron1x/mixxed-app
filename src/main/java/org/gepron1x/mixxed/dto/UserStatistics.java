package org.gepron1x.mixxed.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.gepron1x.mixxed.entity.Mix;

@Data
@Builder
@AllArgsConstructor
public class UserStatistics {

    private final int totalMixes;
    private final int totalPlays;

    private final int totalLikes;
    private final int totalComments;

    private final Mix mostPopularMix;
    private final int mostPopularMixLikes;
    private final int mostPopularMixComments;

}
