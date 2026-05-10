package com.taxi.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopDriverEntry {
    private Long driverId;
    private Double averageScore;
    private Long ratingsCount;
}
