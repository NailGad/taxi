package com.taxi.trip;

import com.taxi.trip.support.TripJwtTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TripControllerJwtAndStatsMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.taxi.trip.client.UserServiceClient userServiceClient;

    @MockBean
    private com.taxi.trip.client.VehicleServiceClient vehicleServiceClient;

    @Test
    void statsDailyWithoutJwtReturns401() throws Exception {
        mockMvc.perform(get("/trips/stats/daily"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void statsDailyWithPassengerJwtReturns200() throws Exception {
        mockMvc.perform(get("/trips/stats/daily")
                        .param("date", LocalDate.of(2035, 6, 10).toString())
                        .header("Authorization", TripJwtTokens.bearerPassenger(5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2035-06-10"))
                .andExpect(jsonPath("$.tripCount").value(0));
    }

    @Test
    void statsDailyWithDriverJwtReturns200() throws Exception {
        mockMvc.perform(get("/trips/stats/daily")
                        .header("Authorization", TripJwtTokens.bearerDriver(3)))
                .andExpect(status().isOk());
    }

    @Test
    void getTripByIdWithoutJwtReturnsForbidden() throws Exception {
        mockMvc.perform(get("/trips/999"))
                .andExpect(status().isForbidden());
    }

    @Test
    void statsDailyWithMalformedJwtReturns401() throws Exception {
        mockMvc.perform(get("/trips/stats/daily")
                        .header("Authorization", "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTripWithoutJwtReturns401() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passengerId\":1,\"origin\":\"A\",\"destination\":\"B\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listTripsWithoutJwtReturns401() throws Exception {
        mockMvc.perform(get("/trips").param("passenger_id", "1"))
                .andExpect(status().isUnauthorized());
    }
}
