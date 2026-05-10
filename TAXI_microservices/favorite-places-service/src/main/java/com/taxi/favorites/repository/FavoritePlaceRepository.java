package com.taxi.favorites.repository;

import com.taxi.favorites.model.FavoritePlace;
import com.taxi.favorites.model.UserKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {
    List<FavoritePlace> findByUserKindAndUserIdOrderByFavoriteDefaultDescIdAsc(UserKind userKind, Long userId);

    List<FavoritePlace> findByUserKindAndUserId(UserKind userKind, Long userId);
}
