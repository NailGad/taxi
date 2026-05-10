package com.taxi.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregateRatingDto {
    private Double averageScore;
    private Long ratingsCount;
}
