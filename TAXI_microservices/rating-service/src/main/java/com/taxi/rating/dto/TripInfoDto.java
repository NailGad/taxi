package com.taxi.rating.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripInfoDto {
    private Long id;
    private Long passengerId;
    private Long driverId;
    private String status;
}
