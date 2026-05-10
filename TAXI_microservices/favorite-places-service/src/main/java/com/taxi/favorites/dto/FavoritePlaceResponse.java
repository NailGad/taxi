package com.taxi.favorites.dto;

import com.taxi.favorites.model.UserKind;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoritePlaceResponse {
    private Long id;
    private UserKind userRole;
    private Long userId;
    private String title;
    private String address;
    private boolean defaultPlace;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
