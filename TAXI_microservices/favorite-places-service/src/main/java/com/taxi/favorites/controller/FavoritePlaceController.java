package com.taxi.favorites.controller;

import com.taxi.favorites.dto.FavoritePlaceRequest;
import com.taxi.favorites.dto.FavoritePlaceResponse;
import com.taxi.favorites.model.UserKind;
import com.taxi.favorites.service.FavoritePlaceBusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
@Slf4j
public class FavoritePlaceController {
    private final FavoritePlaceBusinessService favoritePlaceBusinessService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<FavoritePlaceResponse>> list(
            @PathVariable Long userId,
            @RequestParam(name = "userRole") UserKind userRole) {
        assertFavoriteUser(userId, userRole);
        return ResponseEntity.ok(favoritePlaceBusinessService.list(userId, userRole));
    }

    @PostMapping
    public ResponseEntity<FavoritePlaceResponse> create(@Valid @RequestBody FavoritePlaceRequest body) {
        assertFavoriteUser(body.getUserId(), body.getUserRole());
        FavoritePlaceResponse created = favoritePlaceBusinessService.create(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FavoritePlaceResponse> update(@PathVariable Long id, @Valid @RequestBody FavoritePlaceRequest body) {
        assertFavoriteUser(body.getUserId(), body.getUserRole());
        return ResponseEntity.ok(favoritePlaceBusinessService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam(name = "userRole") UserKind userRole,
            @RequestParam(name = "userId") Long userId) {
        assertFavoriteUser(userId, userRole);
        favoritePlaceBusinessService.delete(id, userRole, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<FavoritePlaceResponse> markDefault(
            @PathVariable Long id,
            @RequestParam(name = "userRole") UserKind userRole,
            @RequestParam(name = "userId") Long userId) {
        assertFavoriteUser(userId, userRole);
        return ResponseEntity.ok(favoritePlaceBusinessService.markDefault(id, userRole, userId));
    }

    private void assertFavoriteUser(Long userId, UserKind role) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.getName().equals(String.valueOf(userId))) {
            throw new AccessDeniedException("JWT subject does not match user");
        }
        String expected = role == UserKind.PASSENGER ? "ROLE_PASSENGER" : "ROLE_DRIVER";
        if (!a.getAuthorities().contains(new SimpleGrantedAuthority(expected))) {
            throw new AccessDeniedException("JWT role does not match userRole");
        }
    }
}
