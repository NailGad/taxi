package com.taxi.favorites.service;

import com.taxi.favorites.client.UserServiceClient;
import com.taxi.favorites.dto.FavoritePlaceRequest;
import com.taxi.favorites.dto.FavoritePlaceResponse;
import com.taxi.favorites.model.FavoritePlace;
import com.taxi.favorites.model.UserKind;
import com.taxi.favorites.repository.FavoritePlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoritePlaceBusinessService {

    private final FavoritePlaceRepository favoritePlaceRepository;
    private final UserServiceClient userServiceClient;

    public List<FavoritePlaceResponse> list(Long userId, UserKind role) {
        return favoritePlaceRepository.findByUserKindAndUserIdOrderByFavoriteDefaultDescIdAsc(role, userId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional
    public FavoritePlaceResponse create(FavoritePlaceRequest req) {
        ensureUser(req.getUserRole(), req.getUserId());
        FavoritePlace fp = FavoritePlace.builder()
                .userKind(req.getUserRole())
                .userId(req.getUserId())
                .title(trim(req.getTitle()))
                .address(trim(req.getAddress()))
                .favoriteDefault(req.isDefaultPlace())
                .build();
        FavoritePlace saved = favoritePlaceRepository.save(fp);
        if (saved.isFavoriteDefault()) {
            clearOthersDefault(saved.getUserKind(), saved.getUserId(), saved.getId());
            saved.setFavoriteDefault(true);
            saved = favoritePlaceRepository.save(saved);
        }
        return map(saved);
    }

    @Transactional
    public FavoritePlaceResponse update(Long id, FavoritePlaceRequest req) {
        FavoritePlace fp = favoritePlaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Favorite place not found: " + id));
        if (!fp.getUserKind().equals(req.getUserRole()) || !fp.getUserId().equals(req.getUserId())) {
            throw new RuntimeException("Ownership mismatch for favorite place");
        }
        ensureUser(req.getUserRole(), req.getUserId());
        fp.setTitle(trim(req.getTitle()));
        fp.setAddress(trim(req.getAddress()));
        fp.setFavoriteDefault(req.isDefaultPlace());
        FavoritePlace saved = favoritePlaceRepository.save(fp);
        if (saved.isFavoriteDefault()) {
            clearOthersDefault(saved.getUserKind(), saved.getUserId(), saved.getId());
            saved.setFavoriteDefault(true);
            saved = favoritePlaceRepository.save(saved);
        }
        return map(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!favoritePlaceRepository.existsById(id)) {
            throw new RuntimeException("Favorite place not found: " + id);
        }
        favoritePlaceRepository.deleteById(id);
    }

    @Transactional
    public FavoritePlaceResponse markDefault(Long id, UserKind role, Long userId) {
        FavoritePlace fp = favoritePlaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Favorite place not found: " + id));
        if (!fp.getUserKind().equals(role) || !fp.getUserId().equals(userId)) {
            throw new RuntimeException("Ownership mismatch for favorite place");
        }
        clearOthersDefault(role, userId, id);
        fp.setFavoriteDefault(true);
        return map(favoritePlaceRepository.save(fp));
    }

    private void clearOthersDefault(UserKind kind, Long userId, Long keepId) {
        List<FavoritePlace> all = favoritePlaceRepository.findByUserKindAndUserId(kind, userId);
        for (FavoritePlace f : all) {
            if (!f.getId().equals(keepId) && f.isFavoriteDefault()) {
                f.setFavoriteDefault(false);
                favoritePlaceRepository.save(f);
            }
        }
    }

    private void ensureUser(UserKind kind, Long userId) {
        boolean ok = kind == UserKind.PASSENGER
                ? userServiceClient.passengerExists(userId)
                : userServiceClient.driverExists(userId);
        if (!ok) {
            throw new RuntimeException(kind == UserKind.PASSENGER ? "Passenger not found: " + userId : "Driver not found: " + userId);
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private FavoritePlaceResponse map(FavoritePlace f) {
        return new FavoritePlaceResponse(
                f.getId(),
                f.getUserKind(),
                f.getUserId(),
                f.getTitle(),
                f.getAddress(),
                f.isFavoriteDefault(),
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }
}
