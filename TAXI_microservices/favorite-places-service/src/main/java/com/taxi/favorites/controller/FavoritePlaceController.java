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
        return ResponseEntity.ok(favoritePlaceBusinessService.list(userId, userRole));
    }

    @PostMapping
    public ResponseEntity<FavoritePlaceResponse> create(@Valid @RequestBody FavoritePlaceRequest body) {
        FavoritePlaceResponse created = favoritePlaceBusinessService.create(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FavoritePlaceResponse> update(@PathVariable Long id, @Valid @RequestBody FavoritePlaceRequest body) {
        return ResponseEntity.ok(favoritePlaceBusinessService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        favoritePlaceBusinessService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<FavoritePlaceResponse> markDefault(
            @PathVariable Long id,
            @RequestParam(name = "userRole") UserKind userRole,
            @RequestParam(name = "userId") Long userId) {
        return ResponseEntity.ok(favoritePlaceBusinessService.markDefault(id, userRole, userId));
    }
}
