package com.taxi.favorites;

import com.taxi.favorites.client.UserServiceClient;
import com.taxi.favorites.dto.FavoritePlaceRequest;
import com.taxi.favorites.dto.FavoritePlaceResponse;
import com.taxi.favorites.model.FavoritePlace;
import com.taxi.favorites.model.UserKind;
import com.taxi.favorites.repository.FavoritePlaceRepository;
import com.taxi.favorites.service.FavoritePlaceBusinessService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoritePlaceBusinessServiceTest {

    @Mock
    private FavoritePlaceRepository favoritePlaceRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private FavoritePlaceBusinessService service;

    @Test
    void createThrowsWhenPassengerMissing() {
        when(userServiceClient.passengerExists(10L)).thenReturn(false);
        var req = new FavoritePlaceRequest(UserKind.PASSENGER, 10L, "Home", "Addr", false);
        assertThatThrownBy(() -> service.create(req))
                .hasMessageContaining("Passenger not found");
    }

    @Test
    void createPersistsWhenPassengerExists() {
        when(userServiceClient.passengerExists(10L)).thenReturn(true);
        var req = new FavoritePlaceRequest(UserKind.PASSENGER, 10L, " Home ", " Addr ", false);
        FavoritePlace saved = FavoritePlace.builder()
                .id(1L)
                .userKind(UserKind.PASSENGER)
                .userId(10L)
                .title("Home")
                .address("Addr")
                .favoriteDefault(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(favoritePlaceRepository.save(any(FavoritePlace.class))).thenReturn(saved);

        FavoritePlaceResponse out = service.create(req);

        assertThat(out.getId()).isEqualTo(1L);
        assertThat(out.getTitle()).isEqualTo("Home");
        verify(userServiceClient).passengerExists(10L);
    }

    @Test
    void createThrowsWhenDriverMissing() {
        when(userServiceClient.driverExists(5L)).thenReturn(false);
        var req = new FavoritePlaceRequest(UserKind.DRIVER, 5L, "Base", "Street", false);
        assertThatThrownBy(() -> service.create(req))
                .hasMessageContaining("Driver not found");
    }

    @Test
    void updateThrowsWhenFavoriteMissing() {
        when(favoritePlaceRepository.findById(99L)).thenReturn(Optional.empty());
        var req = new FavoritePlaceRequest(UserKind.PASSENGER, 1L, "t", "a", false);
        assertThatThrownBy(() -> service.update(99L, req))
                .hasMessageContaining("Favorite place not found");
    }

    @Test
    void updateThrowsWhenOwnershipMismatch() {
        FavoritePlace fp = FavoritePlace.builder()
                .id(1L).userKind(UserKind.PASSENGER).userId(2L)
                .title("x").address("y").favoriteDefault(false).build();
        when(favoritePlaceRepository.findById(1L)).thenReturn(Optional.of(fp));
        var req = new FavoritePlaceRequest(UserKind.PASSENGER, 9L, "t", "a", false);
        assertThatThrownBy(() -> service.update(1L, req))
                .hasMessageContaining("Ownership mismatch");
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(favoritePlaceRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(7L, UserKind.DRIVER, 1L))
                .hasMessageContaining("Favorite place not found");
    }

    @Test
    void deleteThrowsWhenOwnershipMismatch() {
        FavoritePlace fp = FavoritePlace.builder()
                .id(1L).userKind(UserKind.DRIVER).userId(3L)
                .title("x").address("y").favoriteDefault(false).build();
        when(favoritePlaceRepository.findById(1L)).thenReturn(Optional.of(fp));
        assertThatThrownBy(() -> service.delete(1L, UserKind.DRIVER, 99L))
                .hasMessageContaining("Ownership mismatch");
    }

    @Test
    void markDefaultClearsOtherDefaultsForSameUser() {
        FavoritePlace target = FavoritePlace.builder()
                .id(10L).userKind(UserKind.PASSENGER).userId(1L)
                .title("A").address("x").favoriteDefault(false).build();
        FavoritePlace other = FavoritePlace.builder()
                .id(11L).userKind(UserKind.PASSENGER).userId(1L)
                .title("B").address("y").favoriteDefault(true).build();
        when(favoritePlaceRepository.findById(10L)).thenReturn(Optional.of(target));
        when(favoritePlaceRepository.findByUserKindAndUserId(UserKind.PASSENGER, 1L))
                .thenReturn(List.of(target, other));
        when(favoritePlaceRepository.save(any(FavoritePlace.class))).thenAnswer(inv -> inv.getArgument(0));

        FavoritePlaceResponse out = service.markDefault(10L, UserKind.PASSENGER, 1L);

        assertThat(out.isDefaultPlace()).isTrue();
        ArgumentCaptor<FavoritePlace> cap = ArgumentCaptor.forClass(FavoritePlace.class);
        verify(favoritePlaceRepository, atLeast(2)).save(cap.capture());
        assertThat(cap.getAllValues()).anyMatch(f -> f.getId().equals(11L) && !f.isFavoriteDefault());
    }

    @Test
    void listMapsOrderedResults() {
        FavoritePlace a = FavoritePlace.builder()
                .id(1L).userKind(UserKind.PASSENGER).userId(5L)
                .title("t").address("ad").favoriteDefault(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(favoritePlaceRepository.findByUserKindAndUserIdOrderByFavoriteDefaultDescIdAsc(UserKind.PASSENGER, 5L))
                .thenReturn(List.of(a));

        assertThat(service.list(5L, UserKind.PASSENGER)).hasSize(1);
    }
}
