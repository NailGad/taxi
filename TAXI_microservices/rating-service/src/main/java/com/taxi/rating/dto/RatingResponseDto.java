package com.taxi.rating.dto;

import com.taxi.rating.model.RatingDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponseDto {
    private Long id;
    private Long tripId;
    private RatingDirection direction;
    private Integer score;
    private Long raterId;
    private Long rateeId;
    private LocalDateTime createdAt;
}
