package com.taxi.rating.dto;

import com.taxi.rating.model.TripParticipantRole;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRatingRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;

    @NotNull
    private TripParticipantRole raterRole;

    @NotNull
    private Long raterId;
}
