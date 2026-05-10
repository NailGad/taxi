package com.taxi.favorites.dto;

import com.taxi.favorites.model.UserKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoritePlaceRequest {
    @NotNull
    private UserKind userRole;

    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 1000)
    private String address;

    private boolean defaultPlace;
}
